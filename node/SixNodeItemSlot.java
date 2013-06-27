package mods.eln.node;

import mods.eln.Eln;
import mods.eln.ItemStackFilter;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SixNodeItemSlot extends Slot{

	public SixNodeItemSlot(
			IInventory inventory, int slot,
			int x, int y,
			int stackLimit,Class[] descriptorClassList
			)
	{
		super(inventory, slot, x, y);
		this.stackLimit = stackLimit;
		this.descriptorClassList = descriptorClassList;
	}


	Class[] descriptorClassList;
	int stackLimit;
	

	
    /**
     * Check if the stack is a valid item for this slot. Always true beside for the armor slots.
     */
    public boolean isItemValid(ItemStack itemStack)
    {
    	if(itemStack.getItem() != Eln.sixNodeItem) return false;
    	SixNodeDescriptor descriptor = Eln.sixNodeItem.getDescriptor(itemStack);
   	
        for(Class classFilter : descriptorClassList)
        {      	
        	if(descriptor.getClass().equals(classFilter)) return true;
        }
        return false;
    }

    
    @Override
    public int getSlotStackLimit() {
    	// TODO Auto-generated method stub
    	//return super.getSlotStackLimit();
    	return stackLimit;
    }

}
