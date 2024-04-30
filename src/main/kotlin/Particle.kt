import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.joml.Vector3d

class Particle(
    val particleType: Particle = Particle.CLOUD,
    val amount: Int = 10,
    val offset: Vector3d = Vector3d(1.0, 1.0, 1.0),
    val longRange: Boolean = false,

) {
    fun spawnAt(location: Location) {

    }

    fun spawnFor(vararg player: Player) {

    }
}

enum class Faces(
    val top: Boolean,
    val west: Boolean,
    val north: Boolean
) {

}
class ParticleBox(
    val minLocation: Location,
    val maxLocation: Location,
    val particle: Particle = Particle.CLOUD,
    val density: Double = 0.5,
    val faces: listOf()
) {

}