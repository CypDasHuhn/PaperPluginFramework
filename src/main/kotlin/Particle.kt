import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.entity.Entity
import org.joml.Vector3d

class Particle(
    val particleType: Particle = Particle.CLOUD,
    val amount: Int = 10,
    val offset: Vector3d = Vector3d(1.0, 1.0, 1.0),
    val longRange: Boolean = false,

    ) {
    fun spawnAt(vararg locations: Location) {

    }

    fun spawnFor(vararg entities: Entity) {

    }
}

enum class Axis {
    X,
    Y,
    Z
}

enum class Face(
    val axis: Axis,
    val positive: Boolean
) {
    TOP(Axis.Y, true),
    BOTTOM(Axis.Y, false),
    WEST(Axis.X, false),
    EAST(Axis.X, true),
    NORTH(Axis.Z, false),
    SOUTH(Axis.Z, true),
}

class ParticleBox(
) {
    constructor(
        minLocation: Location,
        maxLocation: Location,
        particle: Particle = Particle.CLOUD,
        density: Double = 0.5,
        faces: List<Face> // = default by axis

    ) : this()

    constructor(
        particle: Particle = Particle.CLOUD,
        density: Double = 0.5,
        faces: List<Face> // = default by axis

    ) : this()
}