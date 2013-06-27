package mods.eln.electricasensor;

import mods.eln.BasicContainer;
import mods.eln.electricalcable.ElectricalCableDescriptor;
import mods.eln.item.LampSlot;
import mods.eln.node.SixNodeItemSlot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class ElectricalSensorContainer extends BasicContainer {

	public static final int cableSlotId = 0;
	
	public ElectricalSensorContainer(EntityPlayer player, IInventory inventory) {
		super(player, inventory,new Slot[]{
				new SixNodeItemSlot(inventory,cableSlotId,62 + 0,17,1,new Class[]{ElectricalCableDescriptor.class})
			});
		
		// TODO Auto-generated constructor stub
	}

}
