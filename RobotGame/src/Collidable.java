
/**
 * Represents static entities that can collide with other entities in the way walls do (not damage or knockback).
 * Such entities have a cylindrical collision mesh with their origin at the center of the base
 * of the cylinder.
 * @author Patrick Owen
 */
public interface Collidable
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
}
