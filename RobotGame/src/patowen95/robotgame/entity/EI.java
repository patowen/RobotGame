package patowen95.robotgame.entity;
import patowen95.robotgame.Controller;
import patowen95.robotgame.World;
import patowen95.robotgame.entity.effect.EntityExplosion;
import patowen95.robotgame.entity.effect.EntityFade;
import patowen95.robotgame.entity.enemy.EnemyFortress;
import patowen95.robotgame.entity.enemy.EnemyGrappler;
import patowen95.robotgame.entity.enemy.EnemyObstacle;
import patowen95.robotgame.entity.enemy.EnemyRipper;
import patowen95.robotgame.entity.enemy.EnemyShocking;
import patowen95.robotgame.entity.enemy.EnemySplitting;
import patowen95.robotgame.entity.enemy.EnemyTracking;
import patowen95.robotgame.entity.enemy.EnemyTurret;
import patowen95.robotgame.entity.weapon.EntityBullet;
import patowen95.robotgame.entity.weapon.EntityPlasmaBolt;
import patowen95.robotgame.entity.weapon.EntityRocket;


/**
 * Short for EntityIndex
 * @author Patrick Owen
 */
public class EI
{
	public static final int Player = 0x0000;
	public static final int EnemyObstacle = 0x0001;
	public static final int EnemyTurret = 0x0002;
	public static final int EnemyShocking = 0x0003;
	public static final int EnemyTracking = 0x0004;
	public static final int EnemySplitting = 0x0005;
	public static final int EnemyFortress = 0x0006;
	public static final int EnemyRipper = 0x0007;
	public static final int EnemyGrappler = 0x0008;
	
	public static final int EntityBullet = 0x0100;
	public static final int EntityExplosion = 0x0101;
	public static final int EntityFade = 0x0102;
	public static final int EntityPlasmaBolt = 0x0103;
	public static final int EntityRocket = 0x0104;
	
	private static Entity constructEntity(Controller c, World world, int type)
	{
		switch (type)
		{
		case Player: return new Player(c, world);
		case EnemyObstacle: return new EnemyObstacle(c, world);
		case EnemyTurret: return new EnemyTurret(c, world);
		case EnemyShocking: return new EnemyShocking(c, world);
		case EnemyTracking: return new EnemyTracking(c, world);
		case EnemySplitting: return new EnemySplitting(c, world);
		case EnemyFortress: return new EnemyFortress(c, world);
		case EnemyRipper: return new EnemyRipper(c, world);
		case EnemyGrappler: return new EnemyGrappler(c, world);
		
		case EntityBullet: return new EntityBullet(c, world);
		case EntityExplosion: return new EntityExplosion(c, world);
		case EntityFade: return new EntityFade(c, world);
		case EntityPlasmaBolt: return new EntityPlasmaBolt(c, world);
		case EntityRocket: return new EntityRocket(c, world);
		}
		
		return null;
	}
	
	/**
	 * Initializes an entity with the given id and returns it.
	 * @param world The world where the obstacle is being placed.
	 * @param id The index of the entity.
	 * @return An object of the appropriate kind of Entity.
	 */
	public static Entity createEntity(Controller c, World world, int type)
	{
		Entity e = constructEntity(c, world, type);
		if (e != null)
			e.init(type);
		return e;
	}
	
	/**
	 * Initializes an entity with the given id and returns it.
	 * @param world The world where the obstacle is being placed.
	 * @param id The index of the entity.
	 * @return An object of the appropriate kind of Entity.
	 */
	public static Entity createEntity(Controller c, World world, int type, int owner, int id)
	{
		Entity e = constructEntity(c, world, type);
		if (e != null)
			e.init(type, owner, id);
		return e;
	}
}
