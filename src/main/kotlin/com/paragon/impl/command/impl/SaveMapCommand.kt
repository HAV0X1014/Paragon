package com.paragon.impl.command.impl

import com.paragon.impl.command.Command
import com.paragon.impl.command.syntax.ArgumentData
import com.paragon.impl.command.syntax.SyntaxBuilder
import com.paragon.mixins.accessor.IEntityRenderer
import com.paragon.mixins.accessor.IMapItemRenderer
import com.paragon.mixins.accessor.IMapItemRendererInstance
import com.paragon.util.mc
import com.paragon.util.system.backgroundThread
import kotlinx.coroutines.Job
import net.minecraft.client.renderer.Vector3d
import net.minecraft.entity.item.EntityItemFrame
import net.minecraft.item.ItemMap
import net.minecraft.util.EnumFacing
import net.minecraft.util.text.TextFormatting
import java.awt.image.BufferedImage
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList
import javax.imageio.ImageIO
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * @author SooStrator1136
 */
object SaveMapCommand : Command(
    "SaveMap",
    SyntaxBuilder.createBuilder(arrayListOf(ArgumentData("mode", arrayOf("holding", "frames", "smart"))))
) {

    private lateinit var itemRenderInstances: Map<String, Any>

    private val frameList = CopyOnWriteArrayList<EntityItemFrame>()

    private var currentJob: Job? = null

    override fun call(args: Array<String>, fromConsole: Boolean): Boolean {
        if (args.isEmpty()) {
            return false
        }

        if (currentJob != null && (currentJob ?: return true).isActive) {
            sendMessage("${TextFormatting.RED}Already in the process of saving maps!")
            return true
        }

        @Suppress("IncorrectFormatting")
        itemRenderInstances = ((mc.entityRenderer as IEntityRenderer).mapItemRenderer as IMapItemRenderer).hookGetLoadedMaps()

        when (args[0]) {
            "holding" -> {
                currentJob = backgroundThread {
                    arrayOf(
                        mc.player.heldItemMainhand, mc.player.heldItemOffhand
                    ).forEach {
                        if (it.item !is ItemMap) {
                            return@forEach
                        }

                        saveMapImage(
                            it.displayName, getImageById(
                                ((it.item as ItemMap).getMapData(it, mc.world) ?: return@forEach).mapName
                            )
                        )
                    }
                }

                return true
            }

            "frames" -> {
                currentJob = backgroundThread {
                    mc.world.loadedEntityList.filterIsInstance<EntityItemFrame>().forEach {
                        if (it.displayedItem.item !is ItemMap) {
                            return@forEach
                        }

                        saveMapImage(
                            it.displayedItem.displayName, getImageById(
                                ((it.displayedItem.item as ItemMap).getMapData(
                                    it.displayedItem, mc.world
                                ) ?: return@forEach).mapName
                            )
                        )
                    }

                    sendMessage("${TextFormatting.GREEN}Finished saving all loaded maps!")
                }

                return true
            }

            "smart" -> {
                currentJob = backgroundThread {
                    frameList.addAll(mc.world.loadedEntityList.filterIsInstance<EntityItemFrame>().filter {
                        it.displayedItem.item is ItemMap
                    })

                    manageMaps()

                    frameList.clear()

                    sendMessage("${TextFormatting.GREEN}Finished merging and saving all loaded maps!")
                }

                return true
            }

            else -> {
                return false
            }
        }
    }

    private fun manageMaps() {
        var i = 0
        while (i < frameList.size) {
            val currentFrame = frameList[i]
            val unmergedImages = getMapWithBounds(currentFrame)

            val upperMerged = ArrayList<BufferedImage>()

            repeat(unmergedImages[0].size) { outerIndex ->
                var mergingImage = unmergedImages[0][outerIndex]
                repeat(unmergedImages.size - 1) {
                    mergingImage = mergeImages(mergingImage, unmergedImages[it + 1][outerIndex], 0)
                }
                upperMerged.add(mergingImage)
            }

            var mergedImage = upperMerged[0]

            repeat(upperMerged.size - 1) {
                mergedImage = mergeImages(
                    mergedImage, upperMerged[it + 1], when (currentFrame.facingDirection) {
                        EnumFacing.SOUTH, EnumFacing.WEST -> 1
                        else -> 2
                    }
                )
            }

            saveMapImage(currentFrame.displayedItem.displayName, mergedImage)

            i++
        }
    }

    private fun getMapWithBounds(knownMap: EntityItemFrame): Array<Array<BufferedImage>> {
        val awareFrames = ArrayList<EntityItemFrame>()

        //Sets suck but there is no better way to prevent duplicates in this case
        val unchecked = HashSet<EntityItemFrame>()
        unchecked.add(knownMap)

        val minVec = Vector3d()
        minVec.x = knownMap.posX
        minVec.y = knownMap.posY
        minVec.z = knownMap.posZ

        val maxVec = Vector3d()
        maxVec.x = knownMap.posX
        maxVec.y = knownMap.posY
        maxVec.z = knownMap.posZ

        while (unchecked.isNotEmpty()) {
            val toCheck = unchecked.first()

            minVec.x = min(minVec.x, toCheck.posX)
            minVec.y = min(minVec.y, toCheck.posY)
            minVec.z = min(minVec.z, toCheck.posZ)

            maxVec.x = max(maxVec.x, toCheck.posX)
            maxVec.y = max(maxVec.y, toCheck.posY)
            maxVec.z = max(maxVec.z, toCheck.posZ)

            getMapsNextTo(toCheck).forEach {
                if (it != null && !awareFrames.contains(it)) {
                    unchecked.add(it)
                }
            }

            unchecked.remove(toCheck)
            awareFrames.add(toCheck)
        }

        awareFrames.clear()
        unchecked.clear()

        if (maxVec.x == minVec.x && maxVec.z == minVec.z && maxVec.y == minVec.y) {
            return arrayOf(arrayOf(getMapImageByFrame(knownMap)))
        } else {
            return Array((maxVec.y - minVec.y).roundToInt() + 1) { outerIndex ->
                Array(
                    if (maxVec.x == minVec.x) (maxVec.z - minVec.z).roundToInt() + 1 else (maxVec.x - minVec.x).roundToInt() + 1
                ) { innerIndex ->
                    getMapImageByFrame(getFrameAtPosition(
                        minVec.x + if (maxVec.x == minVec.x) 0 else innerIndex,
                        minVec.y + outerIndex,
                        minVec.z + if (maxVec.x == minVec.x) innerIndex else 0
                    ).also {
                        frameList.remove(it)
                    })
                }
            }
        }
    }

    private fun getMapImageByFrame(frame: EntityItemFrame?): BufferedImage {
        return if (frame != null) {
            getImageById(
                ((frame.displayedItem.item as ItemMap).getMapData(
                    frame.displayedItem, mc.world
                ) ?: return BufferedImage(128, 128, BufferedImage.TYPE_BYTE_GRAY)).mapName
            )
        } else {
            BufferedImage(128, 128, BufferedImage.TYPE_BYTE_GRAY) //Black (placeholder) img
        }
    }

    private fun getMapsNextTo(from: EntityItemFrame) = arrayOf(
        getFrameAtPosition(from.posX, from.posY + 1.0, from.posZ),
        getFrameAtPosition(from.posX, from.posY - 1.0, from.posZ),
        getFrameAtPosition(from.posX + 1, from.posY, from.posZ),
        getFrameAtPosition(from.posX - 1, from.posY, from.posZ),
        getFrameAtPosition(from.posX, from.posY, from.posZ + 1),
        getFrameAtPosition(from.posX, from.posY, from.posZ - 1)
    )

    private fun getFrameAtPosition(x: Double, y: Double, z: Double) = frameList.firstOrNull {
        it.posX == x && it.posY == y && it.posZ == z
    }

    //0 = above, 1 = right, 2 = left //TODO clean this shit
    private fun mergeImages(startImage: BufferedImage, toMerge: BufferedImage, position: Int): BufferedImage {
        return BufferedImage(
            startImage.width + if (position == 1 || position == 2) toMerge.width else 0,
            startImage.height + if (position == 0) toMerge.height else 0,
            BufferedImage.TYPE_INT_RGB
        ).also { mergedImg ->
            val startPixels = IntArray(startImage.width * startImage.height).let {
                startImage.getRGB(
                    0, 0, startImage.width, startImage.height, it, 0, startImage.width
                )
            }
            val toMergePixels = IntArray(toMerge.width * toMerge.height).let {
                toMerge.getRGB(
                    0, 0, toMerge.width, toMerge.height, it, 0, toMerge.width
                )
            }

            when (position) {
                0 -> {
                    mergedImg.setRGB(
                        0, 0, toMerge.width, toMerge.height, toMergePixels, 0, toMerge.width
                    )

                    mergedImg.setRGB(
                        0, toMerge.height, startImage.width, startImage.height, startPixels, 0, startImage.width
                    )
                }

                1, 2 -> {
                    mergedImg.setRGB(
                        0,
                        0,
                        if (position == 1) startImage.width else toMerge.width,
                        if (position == 1) startImage.height else toMerge.height,
                        if (position == 1) startPixels else toMergePixels,
                        0,
                        if (position == 1) startImage.width else toMerge.width
                    )

                    mergedImg.setRGB(
                        if (position == 1) startImage.width else toMerge.width,
                        0,
                        if (position == 1) toMerge.width else startImage.width,
                        if (position == 1) toMerge.height else startImage.height,
                        if (position == 1) toMergePixels else startPixels,
                        0,
                        if (position == 1) toMerge.width else startImage.width
                    )
                }
            }
        }
    }

    private fun getImageById(id: String) = BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB).also {
        it.setRGB(
            0,
            0,
            128,
            128,
            ((itemRenderInstances[id] ?: return@also) as IMapItemRendererInstance).hookGetMapTexture().textureData,
            0,
            128
        )
    }

    private fun saveMapImage(name: String, img: BufferedImage) {
        File("paragon${File.separator}maps").let {
            if (!it.exists()) {
                it.mkdir()
            }
        }

        var saveLoc = File("paragon${File.separator}maps${File.separator}${name}.jpg")
        var index = 0
        while (saveLoc.exists()) {
            index++
            saveLoc = if (index == 1) {
                File("paragon${File.separator}maps${File.separator}${name} (1).jpg")
            } else {
                File(
                    saveLoc.path.substring(
                        0, saveLoc.path.length - (6 + (index - 1).toString().length)
                    ) + "(${index}).jpg"
                )
            }
        }

        ImageIO.write(img, "jpg", saveLoc)
    }

}