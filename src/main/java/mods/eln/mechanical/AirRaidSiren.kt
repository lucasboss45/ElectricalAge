package mods.eln.mechanical

import mods.eln.Eln
import mods.eln.fluid.FuelRegistry
import mods.eln.fluid.PreciseElementFluidHandler
import mods.eln.misc.Direction
import mods.eln.misc.INBTTReady
import mods.eln.misc.LRDU
import mods.eln.misc.Obj3D
import mods.eln.misc.RcInterpolator
import mods.eln.misc.Utils
import mods.eln.node.NodeBase
import mods.eln.node.published
import mods.eln.node.transparent.EntityMetaTag
import mods.eln.node.transparent.TransparentNode
import mods.eln.node.transparent.TransparentNodeDescriptor
import mods.eln.node.transparent.TransparentNodeEntity
import mods.eln.sim.IProcess
import mods.eln.sim.nbt.NbtElectricalGateInput
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import org.lwjgl.opengl.GL11
import java.io.DataInputStream
import java.io.DataOutputStream

class AirRaidSirenDescriptor(baseName: String, obj: Obj3D) :
    SimpleShaftDescriptor(baseName, AirRaidSirenElement::class, AirRaidSirenElement::class, EntityMetaTag.Basic) {

    override val sound = "eln:RotaryEngine"
    override val static = arrayOf(
        obj.getPart("Body_Cylinder.001")
    )


    override val rotating = arrayOf(
        obj.getPart("Shaft")
    )
    override fun preDraw() {
        GL11.glTranslated(-0.5, -1.5, 0.5)
    }


    @Suppress("CanBePrimaryConstructorProperty") // If you do that, it changes the constructor and BLAMO, Crash!
    override val obj: Obj3D = obj

    override fun addInformation(stack: ItemStack, player: EntityPlayer, list: MutableList<String>, par4: Boolean) {
        list.add("An Air Raid Siren")
        list.add("Powered by raw rotation force")
    }
}

class AirRaidSirenElement(node: TransparentNode, desc_: TransparentNodeDescriptor) :
    SimpleShaftElement(node, desc_) {
    val desc = desc_ as AirRaidSirenDescriptor

    var fluidRate = 0f
    var efficiency = 0f
    val airRaidSirenSlowProcess = AirRaidSlowSirenProcess()

    internal val throttle = NbtElectricalGateInput("throttle")

    internal var volume: Float by published(0f)

    inner class AirRaidSlowSirenProcess() : IProcess, INBTTReady {

        override fun process(time: Double) {
            // Do anything at all?
            val target: Float
            val computedEfficiency = if (shaft.rads > 400) {
                // 800 Rads for best sound
                // Reuse the rotary engine curve
                 Math.max(Math.pow(Math.cos(((shaft.rads - 800) / (800 * 1.5f)) * (Math.PI / 2)), 3.0), 0.0)
            } else {
                0.25
            }
            efficiency = computedEfficiency.toFloat()
            val th = if (throttle.connectedComponents.count() > 0) throttle.normalized else 1.0

            val power = fluidRate * tank.heatEnergyPerMilliBucket * efficiency
            shaft.energy += power * time.toFloat()

            volume = if (fluidRate > 0.25) {
                Math.max(0.75f, (power / desc.maxFluidPower).toFloat())
            } else {
                0.0f
            }
        }

    }

    init {
        slowProcessList.add(airRaidSirenSlowProcess)
        electricalLoadList.add(throttle)
    }

    override fun getFluidHandler() = tank

    override fun getElectricalLoad(side: Direction, lrdu: LRDU) = throttle
    override fun getThermalLoad(side: Direction?, lrdu: LRDU?) = null
    override fun getConnectionMask(side: Direction, lrdu: LRDU): Int {
        if (lrdu == LRDU.Down && (side == front.up() || side == front.down())) return NodeBase.maskElectricalGate
        if (lrdu == LRDU.Up && (side == front.up() || side == front.down())) return NodeBase.maskElectricalGate
        if (lrdu == LRDU.Left && (side == front || side == front.back())) return NodeBase.maskElectricalGate
        if (lrdu == LRDU.Right && (side == front || side == front.back())) return NodeBase.maskElectricalGate
        return 0
    }

    override fun onBlockActivated(entityPlayer: EntityPlayer?, side: Direction?, vx: Float, vy: Float, vz: Float) = false

    override fun thermoMeterString(side: Direction?) = Utils.plotPercent(" Eff:", efficiency.toDouble()) + fluidRate.toString() + "mB/s"

    override fun writeToNBT(nbt: NBTTagCompound) {
        super.writeToNBT(nbt)
        tank.writeToNBT(nbt, "tank")
        airRaidSirenSlowProcess.writeToNBT(nbt, "proc")
    }

    override fun readFromNBT(nbt: NBTTagCompound) {
        super.readFromNBT(nbt)
        tank.readFromNBT(nbt, "tank")
        airRaidSirenSlowProcess.readFromNBT(nbt, "proc")
    }

    override fun getWaila(): Map<String, String> {
        val info = mutableMapOf<String, String>()
        info.put("Speed", Utils.plotRads("", shaft.rads))
        info.put("Energy", Utils.plotEnergy("", shaft.energy))
        if (Eln.wailaEasyMode) {
            info.put("Efficiency", Utils.plotPercent("", efficiency.toDouble()))
            info.put("Fuel usage", Utils.plotBuckets("", fluidRate / 1000.0) + "/s")
        }
        return info
    }

    override fun networkSerialize(stream: DataOutputStream) {
        super.networkSerialize(stream)
        stream.writeFloat(volume)
    }
}

class AirRaidSirenRender(entity: TransparentNodeEntity, desc: TransparentNodeDescriptor) : ShaftRender(entity, desc) {
    override val cableRender = Eln.instance.stdCableRenderSignal

    override fun networkUnserialize(stream: DataInputStream) {
        super.networkUnserialize(stream)
        volumeSetting.target = stream.readFloat()
    }
}
