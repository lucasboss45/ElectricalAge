package mods.eln.sixnode.powersocket;

import mods.eln.generic.GenericItemUsingDamageDescriptor;
import mods.eln.item.BrushDescriptor;
import mods.eln.item.ConfigCopyToolDescriptor;
import mods.eln.item.IConfigurable;
import mods.eln.misc.Coordinate;
import mods.eln.misc.Direction;
import mods.eln.misc.LRDU;
import mods.eln.misc.Utils;
import mods.eln.node.AutoAcceptInventoryProxy;
import mods.eln.node.NodeBase;
import mods.eln.node.six.SixNode;
import mods.eln.node.six.SixNodeDescriptor;
import mods.eln.node.six.SixNodeElement;
import mods.eln.node.six.SixNodeElementInventory;
import mods.eln.sim.mna.state.ElectricalLoad;
import mods.eln.sim.core.IProcess;
import mods.eln.sim.thermal.ThermalLoad;
import mods.eln.sim.mna.passive.Resistor;
import mods.eln.sim.nbt.NbtElectricalLoad;
import mods.eln.sim.mna.VoltageStateWatchDog;
import mods.eln.sim.destruct.WorldExplosion;
import mods.eln.sixnode.electricalcable.ElectricalCableDescriptor;
import mods.eln.sixnode.genericcable.GenericCableDescriptor;
import mods.eln.sixnode.lampsupply.LampSupplyElement;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public class PowerSocketElement extends SixNodeElement implements IConfigurable {

    //NodeElectricalGateInput inputGate = new NodeElectricalGateInput("inputGate");
    public PowerSocketDescriptor descriptor;

    public NbtElectricalLoad outputLoad = new NbtElectricalLoad("outputLoad");
    public Resistor loadResistor = new Resistor(null, null);  // Connected in process()
    public IProcess PowerSocketSlowProcess = new PowerSocketSlowProcess();

    private AutoAcceptInventoryProxy acceptingInventory = new AutoAcceptInventoryProxy(
        new SixNodeElementInventory(1, 64, this)
    ).acceptIfEmpty(0, ElectricalCableDescriptor.class);

    public String channel = "Default channel";

    public int paintColor = 0;

    VoltageStateWatchDog voltageWatchdog = new VoltageStateWatchDog();

    public static final byte setChannelId = 1;

    @Override
    public IInventory getInventory() {
        return acceptingInventory.getInventory();
    }

    @Override
    public Container newContainer(Direction side, EntityPlayer player) {
        return new PowerSocketContainer(player, getInventory());
    }

    public PowerSocketElement(SixNode sixNode, Direction side, SixNodeDescriptor descriptor) {
        super(sixNode, side, descriptor);
        electricalLoadList.add(outputLoad);
        electricalComponentList.add(loadResistor);
        slowProcessList.add(PowerSocketSlowProcess);
        loadResistor.highImpedance();
        this.descriptor = (PowerSocketDescriptor) descriptor;

        slowProcessList.add(voltageWatchdog);
        voltageWatchdog
            .set(outputLoad)
            .set(new WorldExplosion(this).cableExplosion());
    }

    class PowerSocketSlowProcess implements IProcess {

        @Override
        public void process(double time) {
            Coordinate local = sixNode.coordonate;
            LampSupplyElement.PowerSupplyChannelHandle handle = null;
            float bestDist = 1e9f;
            List<LampSupplyElement.PowerSupplyChannelHandle> handles = LampSupplyElement.channelMap.get(channel);
            if(handles != null) {
                for(LampSupplyElement.PowerSupplyChannelHandle hdl : handles) {
                    float dist = (float) hdl.element.sixNode.coordonate.trueDistanceTo(local);
                    if(dist < bestDist && dist <= hdl.element.getRange()) {
                        bestDist = dist;
                        handle = hdl;
                    }
                }
            }

            loadResistor.breakConnection();
            loadResistor.highImpedance();
            if(handle != null && handle.element.getChannelState(handle.id)) {
                ItemStack cable = getInventory().getStackInSlot(PowerSocketContainer.cableSlotId);
                if (cable != null) {
                    GenericCableDescriptor desc = (GenericCableDescriptor) GenericCableDescriptor.getDescriptor(cable);
                    loadResistor.connectTo(handle.element.powerLoad, outputLoad);
                    desc.applyTo(loadResistor);
                }
            }
        }
    }

    @Override
    public ElectricalLoad getElectricalLoad(LRDU lrdu, int mask) {
        if (getInventory().getStackInSlot(PowerSocketContainer.cableSlotId) == null) return null;
        return outputLoad;
    }

    @Override
    public ThermalLoad getThermalLoad(LRDU lrdu, int mask) {
        return null;
    }

    @Override
    public int getConnectionMask(LRDU lrdu) {
        if (getInventory().getStackInSlot(PowerSocketContainer.cableSlotId) == null) return 0;
        return NodeBase.MASK_ELECTRICAL_POWER + (1 << NodeBase.MASK_COLOR_CARE_SHIFT) + (paintColor << NodeBase.MASK_COLOR_SHIFT);
    }

    @Override
    public String multiMeterString() {
        return Utils.plotUIP(outputLoad.getU(), outputLoad.getCurrent());
    }

    @Override
    public String thermoMeterString() {
        return null;
    }

    @Override
    public void initialize() {
        setupFromInventory();
    }

    @Override
    protected void inventoryChanged() {
        super.inventoryChanged();
        sixNode.disconnect();
        setupFromInventory();
        sixNode.connect();
        needPublish();
    }

    @Override
    public void destroy(EntityPlayerMP entityPlayer) {
        super.destroy(entityPlayer);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setString("channel", channel);
        nbt.setInteger("color", paintColor);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        channel = nbt.getString("channel");
        paintColor = nbt.getInteger("color");
    }

    void setupFromInventory() {
        ItemStack cableStack = getInventory().getStackInSlot(PowerSocketContainer.cableSlotId);
        if (cableStack != null) {
            GenericCableDescriptor desc = (GenericCableDescriptor) GenericCableDescriptor.getDescriptor(cableStack);
            desc.applyTo(outputLoad);
            voltageWatchdog.setUNominal(desc.electricalNominalVoltage);
        } else {
            voltageWatchdog.setUNominal(10000);
            outputLoad.highImpedance();
        }
    }

    @Override
    public void networkUnserialize(DataInputStream stream) {
        super.networkUnserialize(stream);

        try {
            switch (stream.readByte()) {
                case setChannelId:
                    channel = stream.readUTF();
                    needPublish();
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean hasGui() {
        return true;
    }

    @Override
    public void networkSerialize(DataOutputStream stream) {
        super.networkSerialize(stream);
        try {
            stream.writeUTF(channel);
            Utils.serialiseItemStack(stream, getInventory().getStackInSlot(PowerSocketContainer.cableSlotId));
            stream.writeInt(paintColor);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onBlockActivated(EntityPlayer entityPlayer, Direction side, float vx, float vy, float vz) {
        ItemStack used = entityPlayer.getCurrentEquippedItem();
        if(used != null) {
            GenericItemUsingDamageDescriptor desc = GenericItemUsingDamageDescriptor.getDescriptor(used);
            if(desc != null && desc instanceof BrushDescriptor) {
                BrushDescriptor brush = (BrushDescriptor) desc;
                int color = brush.getColor(used);
                if(color != paintColor && brush.use(used, entityPlayer)) {
                    paintColor = color;
                    sixNode.reconnect();
                }
                return true;
            }
        }

        return acceptingInventory.take(used, this, true, true);
    }

    @Override
    public void readConfigTool(NBTTagCompound compound, EntityPlayer invoker) {
        if(compound.hasKey("powerChannels")) {
            String newChannel = compound.getTagList("powerChannels", 8).getStringTagAt(0);
            if(newChannel != null && newChannel != "") {
                channel = newChannel;
                needPublish();
            }
        }
        if(ConfigCopyToolDescriptor.readCableType(compound, getInventory(), 0, invoker))
            needPublish();
    }

    @Override
    public void writeConfigTool(NBTTagCompound compound, EntityPlayer invoker) {
        NBTTagList list = new NBTTagList();
        list.appendTag(new NBTTagString(channel));
        compound.setTag("powerChannels", list);
        ConfigCopyToolDescriptor.writeCableType(compound, getInventory().getStackInSlot(0));
    }
}
