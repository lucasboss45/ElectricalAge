package mods.eln.generic;

import java.util.List;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Icon;

public class GenericItemBlockUsingDamageDescriptor {
	String iconName;
	Icon iconIndex;
	public String name;
	
	public Item parentItem;
	public int parentItemDamage;	
	
	public GenericItemBlockUsingDamageDescriptor(String name) {
		this.iconName = "eln:" + name.replaceAll(" ", "") ;
		this.name = name;
	}
	
	public NBTTagCompound getDefaultNBT()
	{
		return null;
	}
	public void addInformation(ItemStack itemStack, EntityPlayer entityPlayer, List list, boolean par4)
	{
		
	}
    public void updateIcons(IconRegister iconRegister)
    {
        this.iconIndex = iconRegister.registerIcon(iconName);
    }
	
	public Icon getIcon()
	{
		return iconIndex;
	}
	public String getName(ItemStack stack)
	{
		return name;
	}
	
	
	public void setParent(Item item,int damage)
	{
		this.parentItem = item;
		this.parentItemDamage = damage;
	}
	public ItemStack newItemStack(int size)
	{
		return new ItemStack(parentItem, size, parentItemDamage);
	}	
	

}
