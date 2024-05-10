package com.paragon.impl.ui.windows.impl

import com.paragon.impl.ui.windows.Window
import com.paragon.util.calculations.Timer
import com.paragon.util.render.ColourUtil.integrateAlpha
import com.paragon.util.render.RenderUtil
import com.paragon.util.render.font.FontUtil
import com.paragon.util.roundToNearest
import org.lwjgl.input.Keyboard
import java.awt.Color
import java.awt.Rectangle
import java.util.*

/**
 * @author Surge
 * @since 27/07/2022
 */
class SnakeWindow(x: Float, y: Float, width: Float, height: Float, grabbableHeight: Float) : Window("Snake", x, y, width, height, grabbableHeight) {

    // create snake in the rough middle of the grid
    private val snake = Snake(this, (width / 2f).roundToNearest(8f).toInt(), (width / 2f).roundToNearest(8f).toInt())

    override fun draw(mouseX: Int, mouseY: Int, mouseDelta: Int) {
        super.draw(mouseX, mouseY, mouseDelta)

        // don't show the snake when it's out of bounds
        RenderUtil.pushScissor(x, y + grabbableHeight, width, height - grabbableHeight)
        snake.draw()
        RenderUtil.popScissor()

        // display game over screen if we're dead
        if (snake.isDead()) {
            RenderUtil.drawRect(x, y + grabbableHeight, width, height - grabbableHeight, Color.BLACK.integrateAlpha(150f))
            FontUtil.drawCenteredString("Game Over${System.lineSeparator()}Reached length ${snake.tiles.size}", x + (width / 2f), y + height / 2f, Color.WHITE)
        }
    }

    override fun keyTyped(character: Char, keyCode: Int) {
        if (snake.isDead()) {
            return
        }

        snake.handleInput(keyCode)
    }

    internal class Snake(private val window: SnakeWindow, var x: Int, var y: Int) {

        // snake body
        val tiles = arrayListOf(Tile(window, x, y, 8f, 8f, Color.GREEN))

        // current position of apple
        private var apple: Tile? = null

        // for apple generation
        private val random = Random()

        // to prevent us from going overly fast
        private val timer = Timer()

        // board bounds
        private val bounds = Rectangle(0, 0, window.width.toInt(), window.height.toInt() - window.grabbableHeight.toInt())

        // current direction we are moving in
        private var direction: Direction? = null
            set(value) {
                // haven't started yet, cannot die from hitting self, or not a direction which would cause us to kill ourselves
                if (field == null || tiles.size == 1 || value != field!!.getOpposite()) {
                    field = value
                }
            }

        fun draw() {
            generateApple()

            // 110 milliseconds feels about right. we don't want to move until the player
            // presses a direction key, so don't run if the direction hasn't been set yet
            if (timer.hasMSPassed(110.0) && direction != null && !isDead()) {

                // move according to current direction
                when (direction) {
                    Direction.UP -> y -= 8
                    Direction.DOWN -> y += 8
                    Direction.LEFT -> x -= 8
                    Direction.RIGHT -> x += 8
                    else -> {}
                }

                // check if we are currently over an apple
                checkApple()

                var lastX = x
                var lastY = y

                tiles.forEach { tile ->
                    // store coordinates
                    val cachedX = tile.x
                    val cachedY = tile.y

                    // set current position to the last tile's old position
                    tile.x = lastX
                    tile.y = lastY

                    // set last x and y to the cached values
                    lastX = cachedX
                    lastY = cachedY
                }

                timer.reset()
            }

            // draw
            tiles.forEach { it.draw() }
            apple?.draw()
        }

        fun handleInput(keyCode: Int) {
            // valid direction
            direction = fromCode(keyCode) ?: return
        }

        fun isDead(): Boolean {
            // we have hit our body
            if (tiles.filter { it.x == x && it.y == y && it.colour == Color.GREEN }.size > 1) {
                return true
            }

            // out of bounds
            if (!bounds.contains(x, y, 8, 8)) {
                return true
            }

            return false
        }

        private fun generateApple() {
            // if we want to generate an apple
            if (apple == null) {

                // we use a while loop because there's a chance that we could generate an apple over the body.
                // the apple field is only assigned when it isn't over a body, so it will recursively generate
                // potential positions until one is found that is valid
                while (apple == null) {
                    // generate coordinate
                    val x = (random.nextInt(window.width.toInt() / 8) * 8).coerceIn(0, window.width.toInt() - 8)
                    val y = (random.nextInt(window.width.toInt() / 8) * 8).coerceIn(8, window.width.toInt())

                    // check if coordinates are over any body tiles
                    if (!tiles.any { it.x == x && it.y == y && it.colour == Color.GREEN }) {
                        // assign to new tile object
                        apple = Tile(window, x, y, 8f, 8f, Color.RED)
                    }
                }
            }
        }

        private fun checkApple() {
            // apple exists (just to make sure), and the head is over it
            if (apple != null && x == apple!!.x && y == apple!!.y) {
                // remove apple from board
                apple = null

                // add new body tile to the end
                addTile()
            }
        }

        private fun addTile() {
            // x and y don't really matter, because they are assigned every game tick
            tiles.add(Tile(window, tiles.last().x, tiles.last().y, 8f, 8f, Color.GREEN))
        }

        private fun fromCode(keyCode: Int): Direction? {
            return when (keyCode) {
                Keyboard.KEY_UP, Keyboard.KEY_W -> Direction.UP
                Keyboard.KEY_DOWN, Keyboard.KEY_S -> Direction.DOWN
                Keyboard.KEY_LEFT, Keyboard.KEY_A -> Direction.LEFT
                Keyboard.KEY_RIGHT, Keyboard.KEY_D -> Direction.RIGHT
                else -> null // Shouldn't be reached
            }
        }

    }

    internal class Tile(private val window: SnakeWindow, var x: Int, var y: Int, var width: Float, var height: Float, val colour: Color) {

        fun draw() {
            RenderUtil.drawRect(window.x + x.toFloat(), (window.y + window.grabbableHeight) + y.toFloat(), width, height, colour)
            RenderUtil.drawBorder(window.x + x + 1f, (window.y + window.grabbableHeight) + y + 1f, width - 2f, height - 2f, 1f, colour.darker().darker())
        }

    }

    internal enum class Direction(private val ordinalOfOpposite: Int) {
        UP(1),
        DOWN(0),
        LEFT(3),
        RIGHT(2);

        fun getOpposite(): Direction {
            return Direction.values()[ordinalOfOpposite]
        }
    }

}