
/**
 * Represents static entities that can collide with other entities in the way walls do (not damage or knockback).
 * Such entities have a cylindrical collision mesh with their origin at the center of the base
 * of the cylinder.
 * @author Patrick Owen
 */
public interface Damageable
{	
	/**
	 * Returns the radius of the Entity.
	 */
	public double getRadius();
	
	/**
	 * Returns the height of the Entity.
	 */
	public double getHeight();
	
	/**
	 * Returns the x-position of the Entity.
	 */
	public double getX();
	
	/**
	 * Returns the x-position of the Entity.
	 */
	public double getY();
	
	/**
	 * Returns the x-position of the Entity.
	 */
	public double getZ();
	
	/**
	 * Returns the previous x-coordinate of the entity.
	 */
	public double getXPrevious();
	
	/**
	 * Returns the previous y-coordinate of the entity.
	 */
	public double getYPrevious();
	
	/**
	 * Returns the previous z-coordinate of the entity.
	 */
	public double getZPrevious();
	
	/**
	 * Applies damage to the entity and knocks it back.
	 * @param amount The amount of health loss inflicted.
	 * @param x
	 * @param y
	 * @param z The direction the blast <i>comes from</i>. Should be a unit vector.
	 * @param knockBack The speed that the entity is knocked back.
	 * @param absolute Whether all momentum should be stopped and replaced by the knockback.
	 * If set to false, the knockback is added to the current momentum.
	 */
	public void applyDamage(double amount, double x, double y, double z, double knockBack, boolean absolute);
}
