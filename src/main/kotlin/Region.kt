import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.util.Vector

data class Region(
    val edge1: Location,
    val edge2: Location
) {
    private val world: World by lazy { edge1.world }

    val minX: Int by lazy { edge1.blockX.coerceAtMost(edge2.blockX) }
    val minY: Int by lazy { edge1.blockY.coerceAtMost(edge2.blockY) }
    val minZ: Int by lazy { edge1.blockZ.coerceAtMost(edge2.blockZ) }
    val maxX: Int by lazy { edge1.blockX.coerceAtLeast(edge2.blockX) }
    val maxY: Int by lazy { edge1.blockY.coerceAtLeast(edge2.blockY) }
    val maxZ: Int by lazy { edge1.blockZ.coerceAtLeast(edge2.blockZ) }

    val sizeX: Int by lazy { maxX - minX + 1 }
    val sizeY: Int by lazy { maxY - minY + 1 }
    val sizeZ: Int by lazy { maxZ - minZ + 1 }

    val volume: Int by lazy { sizeX * sizeY * sizeZ }

    val sideSizeX: Int by lazy { sizeY * sizeZ }
    val sideSizeY: Int by lazy { sizeX * sizeZ }
    val sideSizeZ: Int by lazy { sizeX * sizeY }

    fun contains(location: Location): Boolean {
        return edge1.x <= location.x && location.x <= edge2.x &&
                edge1.y <= location.y && location.y <= edge2.y &&
                edge1.z <= location.z && location.z <= edge2.z
    }

    fun contains(region: Region): Boolean {
        return contains(region.edge1) && contains(region.edge2)
    }

    fun contains(entity: Entity): Boolean {
        return contains(entity.location)
    }

    fun intersects(region: Region): Boolean {
        return contains(region.edge1) || contains(region.edge2) ||
                region.contains(edge1) || region.contains(edge2)
    }

    val blocks: List<Block>
        get() {
            val world: World = edge1.world
            return iterateRegion { x, y, z ->
                world.getBlockAt(x, y, z)
            }
        }

    val blocksArray: Array<Array<Array<Block?>>>
        get() {
            val world: World = edge1.world

            return iterateRegion { x, y, z ->
                world.getBlockAt(x, y, z)
            }.toBlocksArray(this)
        }

    val entities: List<Entity>
        get() {
            return entities()
        }

    fun entities(vararg types: EntityType): List<Entity> {
        val world: World = edge1.world
        return iterateRegion { x, y, z ->
            world.getNearbyEntities(Location(world, x.toDouble(), y.toDouble(), z.toDouble()), 1.0, 1.0, 1.0)
                .filter { entity ->
                    types.isEmpty() || types.contains(entity.type)
                }
        }.flatten()
    }

    val players: List<Player>
        get() {
            return entities(EntityType.PLAYER) as List<Player>
        }

    private inline fun <T> iterateRegion(
        blockSupplier: (Int, Int, Int) -> T
    ): List<T> {
        val items = mutableListOf<T>()
        for (x in minX..maxX) {
            for (y in minY..maxY) {
                for (z in minZ..maxZ) {
                    val item = blockSupplier(x, y, z)
                    items.add(item)
                }
            }
        }
        return items
    }

    val chunks: Set<Chunk> by lazy {
        val world = edge1.world

        val minXChunk = minX / 16
        val minZChunk = minZ / 16
        val maxXChunk = maxX / 16
        val maxZChunk = maxZ / 16

        val chunks = mutableSetOf<Chunk>()
        for (x in minXChunk..maxXChunk) {
            for (z in minZChunk..maxZChunk) {
                chunks.add(world.getChunkAt(x, z))
            }
        }
        chunks
    }

    val chunksFull: Set<Chunk> by lazy {
        val world = edge1.world

        val minXChunk = minX / 16
        val minZChunk = minZ / 16
        val maxXChunk = maxX / 16
        val maxZChunk = maxZ / 16

        val chunks = mutableSetOf<Chunk>()
        for (x in minXChunk..maxXChunk) {
            for (z in minZChunk..maxZChunk) {
                val chunk = world.getChunkAt(x, z)
                if (isChunkFullyContained(chunk)) {
                    chunks.add(chunk)
                }
            }
        }
        chunks
    }

    private fun isChunkFullyContained(chunk: Chunk): Boolean {
        val chunkMinX = chunk.x / 16
        val chunkMinZ = chunk.z / 16
        val chunkMaxX = chunkMinX + 15
        val chunkMaxZ = chunkMinZ + 15

        return minX <= chunkMinX && maxX >= chunkMaxX && minZ <= chunkMinZ && maxZ >= chunkMaxZ
    }

    fun enlarge(amount: Int, vararg axes: Axis): Region {
        val faces = Face.values().filter { axes.contains(it.axis) }
        return enlarge(amount, *faces.toTypedArray())
    }

    fun enlarge(amount: Int, vararg faces: Face): Region {
        return changeBorders(amount, true, *faces)
    }

    fun shrink(amount: Int, vararg axes: Axis): Region {
        val faces = Face.values().filter { axes.contains(it.axis) }
        return shrink(amount, *faces.toTypedArray())
    }

    fun shrink(amount: Int, vararg faces: Face): Region {
        return changeBorders(amount, false, *faces)
    }

    private fun changeBorders(amount: Int, enlarge: Boolean, vararg faces: Face): Region {
        var minEdge = Location(world, minX.toDouble(), minY.toDouble(), minZ.toDouble())
        var maxEdge = Location(world, maxX.toDouble(), maxY.toDouble(), maxZ.toDouble())

        val allFaces = if (faces.isEmpty()) Face.values() else faces
        allFaces.forEach { face ->
            val coordinateChange = amount * (if (enlarge xor face.positive) 1 else -1)
            val changingEdge = if (enlarge xor face.positive) maxEdge else minEdge
            val modifiedEdge = when (face.axis) {
                Axis.X -> changingEdge.add(Vector(coordinateChange, 0, 0))
                Axis.Y -> changingEdge.add(Vector(0, coordinateChange, 0))
                Axis.Z -> changingEdge.add(Vector(0, 0, coordinateChange))
            }
            if (enlarge xor face.positive) maxEdge = modifiedEdge else minEdge = modifiedEdge
        }

        return Region(minEdge, maxEdge)
    }
}

fun List<Block>.toBlocksArray(region: Region): Array<Array<Array<Block?>>> {
    val blocksArray = Array(region.sizeX) { Array(region.sizeY) { arrayOfNulls<Block>(region.sizeZ) } }

    forEachIndexed { index, block ->
        val x = index % region.sizeX
        val y = (index / region.sizeX) % region.sizeY
        val z = index / (region.sizeX * region.sizeY)
        blocksArray[x][y][z] = block
    }
    return blocksArray
}