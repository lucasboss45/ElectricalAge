package mods.eln.heatfurnace;

import mods.eln.BasicContainer;
import mods.eln.Eln;
import mods.eln.generic.GenericItemUsingDamageSlot;
import mods.eln.item.CombustionChamber;
import mods.eln.item.ThermalIsolatorElement;
import mods.eln.item.regulator.IRegulatorDescriptor;
import mods.eln.item.regulator.RegulatorSlot;
import mods.eln.node.INodeContainer;
import mods.eln.node.Node;
import mods.eln.sim.RegulatorType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class HeatFurnaceContainer extends BasicContainer implements INodeContainer{

	public static final int combustibleId = 0;
	public static final int regulatorId = 1;
	public static final int isolatorId = 2;
	public static final int combustrionChamberId = 3;
	
	Node node;
	public HeatFurnaceContainer(Node node,EntityPlayer player, IInventory inventory,HeatFurnaceDescriptor descriptor) {
		super(player, inventory,new Slot[]{
				new Slot(inventory,combustibleId,62 +  0,17),
			//	new RegulatorSlot(inventory,regulatorId,62 +  0,17+18,1,new RegulatorType[]{),
				new GenericItemUsingDamageSlot(inventory,regulatorId, 62 +  0,17+18,1, IRegulatorDescriptor.class),
				new GenericItemUsingDamageSlot(inventory, isolatorId, 62 +  18,17 + 18,1, ThermalIsolatorElement.class),
				new GenericItemUsingDamageSlot(inventory, combustrionChamberId, 62 +  36,17 + 18,descriptor.combustionChamberMax, CombustionChamber.class),
			});
		this.node = node;
		// TODO Auto-generated constructor stub
	}
	@Override
	public Node getNode() {
		// TODO Auto-generated method stub
		return node;
	}
	@Override
	public int getRefreshRateDivider() {
		// TODO Auto-generated method stub
		return 0;
	}

}
