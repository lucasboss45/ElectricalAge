package mods.eln.sim

import mods.eln.Eln
import mods.eln.debug.DP
import mods.eln.debug.DPType
import mods.eln.entity.ReplicatorEntity
import mods.eln.misc.Coordinate
import mods.eln.misc.Utils
import mods.eln.sim.core.IProcess
import net.minecraft.entity.boss.EntityWither
import net.minecraft.entity.monster.EntityEnderman
import net.minecraft.entity.monster.EntityMob

class MonsterPopFreeProcess(private val coordinate: Coordinate, private val range: Int) : IProcess {

    internal var timerCounter = 0.0
    internal val timerPeriod = 0.212

    internal var oldList: List<*>? = null

    override fun process(time: Double) {
        //Monster killing must be active before continuing :
        if (!Eln.killMonstersAroundLamps)
            return

        timerCounter += time
        if (timerCounter > timerPeriod) {
            timerCounter -= Utils.rand(1.0, 1.5) * timerPeriod
            val list = coordinate.world().getEntitiesWithinAABB(EntityMob::class.java, coordinate.getAxisAlignedBB(range + 8))

            for (o in list) {
                //Utils.println("MonsterPopFreeProcess : In range");
                val mob = o as EntityMob
                if (oldList == null || !oldList!!.contains(o)) {
                    if (coordinate.distanceTo(mob) < range) {
                        //Utils.println("MonsterPopFreeProcess : Must die");
                        if (o !is ReplicatorEntity && o !is EntityWither && o !is EntityEnderman) {
                            mob.setDead()
                            DP.println(DPType.MNA, "MonsterPopFreeProcess : Dead")
                        }
                    }
                }
            }
            oldList = list
        }
    }
}
