import java.util.ArrayList;


/**
 * Handles the mathematics of collision checking in the game.
 * @author Patrick Owen
 */
public class Collision
{
	private GameMap map;
	
	private double normalX, normalY, normalZ;
	private double normalXFinal, normalYFinal, normalZFinal;
	
	//Environment data
	private int colData;
	private ArrayList<Double> cX1, cY1, cZ1, cX2, cY2, cZ2, cX3, cY3, cZ3;
	
	/**
	 * Initializes the Collision class.
	 */
	public Collision(GameMap gameMap)
	{
		//Initialize all data arrays
		colData = 0;
		cX1 = new ArrayList<Double>(); cY1 = new ArrayList<Double>(); cZ1 = new ArrayList<Double>();
		cX2 = new ArrayList<Double>(); cY2 = new ArrayList<Double>(); cZ2 = new ArrayList<Double>();
		cX3 = new ArrayList<Double>(); cY3 = new ArrayList<Double>(); cZ3 = new ArrayList<Double>();
		
		map = gameMap;
	}
	
	/**
	 * Returns the x-component of the normal of the wall that stopped an entity since the last call to one of the appropriate methods.
	 */
	public double getNormalX()
	{
		return normalXFinal;
	}
	
	/**
	 * Returns the y-component of the normal of the wall that stopped an entity since the last call to one of the appropriate methods.
	 */
	public double getNormalY()
	{
		return normalYFinal;
	}
	
	/**
	 * Returns the z-component of the normal of the wall that stopped an entity since the last call to one of the appropriate methods.
	 */
	public double getNormalZ()
	{
		return normalZFinal;
	}
	
	/**
	 * Adds a triangle to the list of static surfaces that can be collided with.
	 * @param x1
	 * @param y1
	 * @param z1 First vertex
	 * @param x2
	 * @param y2
	 * @param z2 Second vertex
	 * @param x3
	 * @param y3
	 * @param z3 Third vertex
	 */
	public void addWall(double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3)
	{
		cX1.add(x1); cY1.add(y1); cZ1.add(z1);
		cX2.add(x2); cY2.add(y2); cZ2.add(z2);
		cX3.add(x3); cY3.add(y3); cZ3.add(z3);
		colData += 1;
	}
	
	/**
	 * Returns the value from 0 to 1 for the part of the path between (bx1, by1, bz1)
	 * and (bx+bxd, by+byd, bz+bzd) that triggers a collision between a bullet and the environment.
	 * If 1 is returned, there is no collision.
	 * @param bx1
	 * @param by1
	 * @param bz1 Initial bullet location.
	 * @param bxd
	 * @param byd
	 * @param bzd Bullet displacement.
	 */
	public double getBulletCollision(double bx1, double by1, double bz1, double bxd, double byd, double bzd)
	{
		//The algorithm for this is similar to the player collision one but with simplifications (point instead of cylinder).
		double buffer = 0.001;
		normalXFinal = 0; normalYFinal = 0; normalZFinal = 0;
		if (bxd == 0 && byd == 0 && bzd == 0) return 1;
		double bd = Math.sqrt(sqr(bxd) + sqr(byd) + sqr(bzd)); buffer /= bd;
		bxd *= 1+buffer; byd *= 1+buffer; bzd *= 1+buffer;
		double bxMin = Math.min(bx1,bx1+bxd), byMin = Math.min(by1,by1+byd), bzMin = Math.min(bz1,bz1+bzd);
		double bxMax = Math.max(bx1,bx1+bxd), byMax = Math.max(by1,by1+byd), bzMax = Math.max(bz1,bz1+bzd);
		
		double tReturn = 1;
		double nx = 0, ny = 0, nz = 0;
		
		for (int i=0; i<colData; i+=1)
		{
			double x1 = cX1.get(i), y1 = cY1.get(i), z1 = cZ1.get(i);
			double x2 = cX2.get(i), y2 = cY2.get(i), z2 = cZ2.get(i);
			double x3 = cX3.get(i), y3 = cY3.get(i), z3 = cZ3.get(i);
			
			double xMin = min3(x1,x2,x3), yMin = min3(y1,y2,y3), zMin = min3(z1,z2,z3);
			double xMax = max3(x1,x2,x3), yMax = max3(y1,y2,y3), zMax = max3(z1,z2,z3);
			
			if (bxMax < xMin || bxMin > xMax) continue;
			if (byMax < yMin || byMin > yMax) continue;
			if (bzMax < zMin || bzMin > zMax) continue;
			
			double t;
			
			//Interior
			t = getBTriangleCollision(bx1,by1,bz1,bxd,byd,bzd, x1,y1,z1,x2,y2,z2,x3,y3,z3);
			if (t<tReturn) {tReturn=t; nx=normalX; ny=normalY; nz=normalZ;}
		}
		
		if (tReturn == 1) return 1;
		normalXFinal = nx; normalYFinal = ny; normalZFinal = nz;
		return Math.max(0,tReturn*(1+buffer)-buffer);
	}
	
	/**
	 * Returns the value from 0 to 1 for the part of the path between (bx1, by1, bz1)
	 * and (bx+bxd, by+byd, bz+bzd) that triggers a collision between a cylindrical entity and the environment.
	 * If 1 is returned, there is no collision.
	 * @param px1
	 * @param py1
	 * @param pz1 Initial entity location.
	 * @param pxd
	 * @param pyd
	 * @param pzd Entity displacement.
	 * @param radius
	 * @param height Player dimensions
	 */
	public double getPlayerCollision(double px1, double py1, double pz1, double pxd, double pyd, double pzd, double radius, double height)
	{
		double buffer = 0.001;
		normalXFinal = 0; normalYFinal = 0; normalZFinal = 0;
		if (pxd == 0 && pyd == 0 && pzd == 0) return 1;
		double pd = Math.sqrt(sqr(pxd) + sqr(pyd) + sqr(pzd)); buffer /= pd;
		pxd *= 1+buffer; pyd *= 1+buffer; pzd *= 1+buffer;
		
		//The minimum and maximum x, y, and z values covered by the player
		double pxMin = Math.min(px1,px1+pxd), pyMin = Math.min(py1,py1+pyd), pzMin = Math.min(pz1,pz1+pzd);
		double pxMax = Math.max(px1,px1+pxd), pyMax = Math.max(py1,py1+pyd), pzMax = Math.max(pz1,pz1+pzd);
		
		double tReturn = 1;
		double nx = 0, ny = 0, nz = 0;
		
		//Check for collision with environment walls.
		for (int i=0; i<colData; i+=1)
		{
			/*
			 * ALGORITHM 2a:
			 * (A slight modification in player distances and values to return is indicated by a buffer variable that just prevents floating point errors from allowing
			 *   the player to leak through walls. Their effect on the algorithm is limited and will not be discussed.)
			 * 
			 * Retrieve wall collision and check whether the box covering the wall collides with a box fully surrounding the player's path. If it does not,
			 *   the player cannot possibly collide with the wall, and no further calculation needs to be done, hence the continue statements.
			 * 
			 * Separately check how far the player can go before colliding with the interior, edges, and vertices of each wall (triangle).
			 *   The separations are required because the math involved differs for each element.
			 * 
			 * Make sure that the t-value to return is that of the closest wall.
			 */
			double x1 = cX1.get(i), y1 = cY1.get(i), z1 = cZ1.get(i);
			double x2 = cX2.get(i), y2 = cY2.get(i), z2 = cZ2.get(i);
			double x3 = cX3.get(i), y3 = cY3.get(i), z3 = cZ3.get(i);
			
			double xMin = min3(x1,x2,x3), yMin = min3(y1,y2,y3), zMin = min3(z1,z2,z3);
			double xMax = max3(x1,x2,x3), yMax = max3(y1,y2,y3), zMax = max3(z1,z2,z3);
			
			if (pxMax+radius < xMin || pxMin-radius > xMax) continue;
			if (pyMax+radius < yMin || pyMin-radius > yMax) continue;
			if (pzMax+height < zMin || pzMin > zMax) continue;
			
			double t;
			
			//Interior
			t = getPTriangleCollision(px1,py1,pz1,pxd,pyd,pzd, radius,height, x1,y1,z1,x2,y2,z2,x3,y3,z3);
			if (t<tReturn) {tReturn=t; nx=normalX; ny=normalY; nz=normalZ;}
			//Edges
			t = getPLineCollision(px1,py1,pz1,pxd,pyd,pzd,radius,height, x1,y1,z1,x2-x1,y2-y1,z2-z1);
			if (t<tReturn) {tReturn=t; nx=normalX; ny=normalY; nz=normalZ;}
			t = getPLineCollision(px1,py1,pz1,pxd,pyd,pzd,radius,height, x2,y2,z2,x3-x2,y3-y2,z3-z2);
			if (t<tReturn) {tReturn=t; nx=normalX; ny=normalY; nz=normalZ;}
			t = getPLineCollision(px1,py1,pz1,pxd,pyd,pzd,radius,height, x3,y3,z3,x1-x3,y1-y3,z1-z3);
			if (t<tReturn) {tReturn=t; nx=normalX; ny=normalY; nz=normalZ;}
			//Vertices
			t = getPPointCollision(px1,py1,pz1,pxd,pyd,pzd,radius,height, x1,y1,z1);
			if (t<tReturn) {tReturn=t; nx=normalX; ny=normalY; nz=normalZ;}
			t = getPPointCollision(px1,py1,pz1,pxd,pyd,pzd,radius,height, x2,y2,z2);
			if (t<tReturn) {tReturn=t; nx=normalX; ny=normalY; nz=normalZ;}
			t = getPPointCollision(px1,py1,pz1,pxd,pyd,pzd,radius,height, x3,y3,z3);
			if (t<tReturn) {tReturn=t; nx=normalX; ny=normalY; nz=normalZ;}
		}
		
		//Check for collision with entities.
		for (Entity entity : map.getEntities())
		{
			/*
			 * ALGORITHM 2b:
			 * Check whether the entity in question is one that collides with the player (few do).
			 * 
			 * Again, check to make sure the boxes surrounding the paths of the entities that may be colliding overlap, since the
			 *   entities cannot collide otherwise, and the calculations are quicker.
			 * 
			 * Check how far the player needs to go to collide with the cylinders collide. Checking whether an upright cylinder (the player collision shape and entity shape) collides
			 *   with another upright cylinder is equivalent to check whether a cylinder with the radii added collides with a line segment. The caps of cylinders can also be treated
			 *   like the endpoints of the line segment.
			 * 
			 * Return the shortest distance and store the normals of the wall with the shortest distance.
			 */
			if (!(entity instanceof Collidable)) continue;
			
			Collidable e = (Collidable)entity;
			
			double xMin = e.getX()-e.getRadius(), yMin = e.getY()-e.getRadius(), zMin = e.getZ();
			double xMax = e.getX()+e.getRadius(), yMax = e.getY()+e.getRadius(), zMax = e.getZ()+e.getHeight();
			
			if (pxMax+radius < xMin || pxMin-radius > xMax) continue;
			if (pyMax+radius < yMin || pyMin-radius > yMax) continue;
			if (pzMax+height < zMin || pzMin > zMax) continue;
			
			double t;
			
			//Surface
			t = getPLineCollisionVertical(px1,py1,pz1,pxd,pyd,pzd,radius+e.getRadius(),height,
					e.getX(),e.getY(),e.getZ(), 0,0,e.getHeight());
			if (t<tReturn) {tReturn=t; nx=normalX; ny=normalY; nz=normalZ;}
			
			//Bases
			t = getPPointCollision(px1,py1,pz1,pxd,pyd,pzd,radius+e.getRadius(),height,
					e.getX(), e.getY(), e.getZ());
			if (t<tReturn) {tReturn=t; nx=normalX; ny=normalY; nz=normalZ;}
			t = getPPointCollision(px1,py1,pz1,pxd,pyd,pzd,radius+e.getRadius(),height,
					e.getX(), e.getY(), e.getZ()+e.getHeight());
			if (t<tReturn) {tReturn=t; nx=normalX; ny=normalY; nz=normalZ;}
		}
		
		if (tReturn == 1) return 1;
		normalXFinal = nx; normalYFinal = ny; normalZFinal = nz;
		return Math.max(0,tReturn*(1+buffer)-buffer);
	}
	
	/**
	 * Returns the value from 0 to 1 for the part of the path between (bx1, by1, bz1)
	 * and (bx1+bxd, by1+byd, bz1+bzd) that triggers a collision between a bullet and an entity.
	 * If 1 is returned, there is no collision.
	 * @param bx1
	 * @param by1
	 * @param bz1 Initial bullet location.
	 * @param bxd
	 * @param byd
	 * @param bzd Bullet displacement.
	 * @param px1
	 * @param py1
	 * @param pz1 Player location.
	 * @param pxd
	 * @param pyd
	 * @param pzd Player displacement.
	 * @param radius
	 * @param height Player dimensions.
	 */
	public double getEntityBulletCollision(double bx1, double by1, double bz1, double bxd, double byd, double bzd,
			double px1, double py1, double pz1, double pxd, double pyd, double pzd, double radius, double height)
	{
		if ((bx1-px1)*(bx1-px1) + (by1-py1)*(by1-py1) <= radius*radius && bz1 >= pz1 && bz1 <= pz1+height)
			return 0;
		return getPPointCollision(px1, py1, pz1, pxd-bxd, pyd-byd, pzd-bzd, radius, height, bx1, by1, bz1);
	}
	
	//Returns the value from 0 to 1 for the bullet collision with a plane.
	private double getBTriangleCollision(double bx1, double by1, double bz1, double bxd, double byd, double bzd,
			double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3)
	{
		//Find normal of plane.
		double nx = (y2-y1)*(z3-z1) - (y3-y1)*(z2-z1); double nxO = nx;
		double ny = (z2-z1)*(x3-x1) - (z3-z1)*(x2-x1); double nyO = ny;
		double nz = (x2-x1)*(y3-y1) - (x3-x1)*(y2-y1); double nzO = nz;
		
		double dot = nx*bxd + ny*byd + nz*bzd;
		if (dot == 0) return 1;
		if (dot > 0) {nx = -nx; ny = -ny; nz = -nz; dot = -dot;}
		
		//Find t
		double d = -nx*x1 - ny*y1 - nz*z1;
		double t = (-d-nx*bx1-ny*by1-nz*bz1) / dot;
		
		//Find normals.
		double dist = Math.sqrt(sqr(nx) + sqr(ny) + sqr(nz));
		normalX = nx/dist; normalY = ny/dist; normalZ = nz/dist;
		
		//See whether the point is inside the triangle.
		double xx = bx1 + bxd*t, yy = by1 + byd*t, zz = bz1 + bzd*t;
		double n;
		
		if (Math.abs(nxO) > Math.abs(nyO) && Math.abs(nxO) > Math.abs(nzO)) //Set x to 0
		{
			n = sign(nxO);
			if (sign((y2-y1)*(zz-z1) - (yy-y1)*(z2-z1)) != n) return 1;
			if (sign((y3-y2)*(zz-z2) - (yy-y2)*(z3-z2)) != n) return 1;
			if (sign((y1-y3)*(zz-z3) - (yy-y3)*(z1-z3)) != n) return 1;
		}
		else if (Math.abs(nyO) > Math.abs(nzO)) //Set y to 0
		{
			n = sign(nyO);
			if (sign((z2-z1)*(xx-x1) - (zz-z1)*(x2-x1)) != n) return 1;
			if (sign((z3-z2)*(xx-x2) - (zz-z2)*(x3-x2)) != n) return 1;
			if (sign((z1-z3)*(xx-x3) - (zz-z3)*(x1-x3)) != n) return 1;
		}
		else //Set z to 0
		{
			n = sign(nzO);
			if (sign((x2-x1)*(yy-y1) - (xx-x1)*(y2-y1)) != n) return 1;
			if (sign((x3-x2)*(yy-y2) - (xx-x2)*(y3-y2)) != n) return 1;
			if (sign((x1-x3)*(yy-y3) - (xx-x3)*(y1-y3)) != n) return 1;
		}
		
		if (t<0 || t>=1) return 1;
		return t;
	}
	
	//Returns the value from 0 to 1 for the player collision with a plane.
	//It does not handle edges well. Use another script for that.
	private double getPTriangleCollision(double px1, double py1, double pz1, double pxd, double pyd, double pzd, double radius, double height,
			double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3)
	{
		/*
		 * ALGORITHM 2c:
		 * Use the cross product to find the normal of the plane to find the normal and store it twice (One will be modified, and the original
		 *   normal values are needed later)
		 * Check to make sure the normal vector is facing opposing the player's movement direction (using dot product. If it is not, reverse its direction.
		 *   Store the dot product of the player movement vector and the normal vector.
		 * Find the point of the player cylinder that collides with the wall (global coordinates).
		 * Find how far the contact point must travel before colliding with the plane containing the wall.
		 * Check to make sure that the contact point is inside the triangle when colliding with the plane. Do the following to check:
		 *    Map the triangle onto the coordinate plane that makes the triangle as big as possible (minimize floating point errors).
		 *    Check which side of each triangle edge the contact point is in (by taking a cross product of the edge vector and a vector from one vertex to the
		 *      contact point and checking its sign in relation to the original normal vectors).
		 *    If the point is on the interior side of each edge, the collision is confirmed (as long as the player does not have to go backwards or move farther than its intended distance).
		 */
		//Find normal of plane.
		double nx = (y2-y1)*(z3-z1) - (y3-y1)*(z2-z1); double nxO = nx;
		double ny = (z2-z1)*(x3-x1) - (z3-z1)*(x2-x1); double nyO = ny;
		double nz = (x2-x1)*(y3-y1) - (x3-x1)*(y2-y1); double nzO = nz;
		
		double dot = nx*pxd + ny*pyd + nz*pzd;
		if (dot == 0) return 1;
		if (dot > 0) {nx = -nx; ny = -ny; nz = -nz; dot = -dot;}
		
		//Contact point
		double cx, cy, cz, cz1, cz2;
		double dist = Math.sqrt(sqr(nx) + sqr(ny));
		if (dist == 0) {cx = px1; cy = py1;}
		else {cx = px1 - nx/dist*radius; cy = py1 - ny/dist*radius;}
		cz1 = pz1; cz2 = pz1+height;
		
		//Find t
		double t;
		double d = -nx*x1 - ny*y1 - nz*z1;
		double t1 = (-d-nx*cx-ny*cy-nz*cz1) / dot;
		double t2 = (-d-nx*cx-ny*cy-nz*cz2) / dot;
		if (t1 < t2) {t = t1; cz = cz1;}
		else if (t1 > t2) {t = t2; cz = cz2;}
		else {t = t1; cz=cz1+height/2;}
		
		//Find normals.
		dist = Math.sqrt(sqr(nx) + sqr(ny) + sqr(nz));
		normalX = nx/dist; normalY = ny/dist; normalZ = nz/dist;
		
		//See whether the point is inside the triangle.
		double xx = cx + pxd*t, yy = cy + pyd*t, zz = cz + pzd*t;
		double n;
		
		if (Math.abs(nxO) > Math.abs(nyO) && Math.abs(nxO) > Math.abs(nzO)) //Set x to 0
		{
			n = sign(nxO);
			if (sign((y2-y1)*(zz-z1) - (yy-y1)*(z2-z1)) != n) return 1;
			if (sign((y3-y2)*(zz-z2) - (yy-y2)*(z3-z2)) != n) return 1;
			if (sign((y1-y3)*(zz-z3) - (yy-y3)*(z1-z3)) != n) return 1;
		}
		else if (Math.abs(nyO) > Math.abs(nzO)) //Set y to 0
		{
			n = sign(nyO);
			if (sign((z2-z1)*(xx-x1) - (zz-z1)*(x2-x1)) != n) return 1;
			if (sign((z3-z2)*(xx-x2) - (zz-z2)*(x3-x2)) != n) return 1;
			if (sign((z1-z3)*(xx-x3) - (zz-z3)*(x1-x3)) != n) return 1;
		}
		else //Set z to 0
		{
			n = sign(nzO);
			if (sign((x2-x1)*(yy-y1) - (xx-x1)*(y2-y1)) != n) return 1;
			if (sign((x3-x2)*(yy-y2) - (xx-x2)*(y3-y2)) != n) return 1;
			if (sign((x1-x3)*(yy-y3) - (xx-x3)*(y1-y3)) != n) return 1;
		}
		
		if (t<0 || t>=1) return 1;
		return t;
	}
	
	//Returns the value from 0 to 1 for the player collision with a line segment.
	//It does not handle endpoints well. Use another script for that.
	private double getPLineCollision(double px1, double py1, double pz1, double pxd, double pyd, double pzd, double radius, double height,
			double x1, double y1, double z1, double xd, double yd, double zd)
	{
		if (zd == 0)
			return getPLineCollisionHorizontal(px1,py1,pz1,pxd,pyd,pzd,radius,height,x1,y1,z1,xd,yd,zd);
		else if (xd == 0 && yd == 0)
			return getPLineCollisionVertical(px1,py1,pz1,pxd,pyd,pzd,radius,height,x1,y1,z1,xd,yd,zd);
		else
			return getPLineCollisionSlanted(px1,py1,pz1,pxd,pyd,pzd,radius,height,x1,y1,z1,xd,yd,zd);
	}
	
	//Returns the value from 0 to 1 for the player collision with a slanted line segment.
	//It does not handle endpoints well. Use another script for that.
	private double getPLineCollisionSlanted(double px1, double py1, double pz1, double pxd, double pyd, double pzd, double radius, double height,
			double x1, double y1, double z1, double xd, double yd, double zd)
	{
		/*
		 * ALGORITHM 2d:
		 * Solve a system of equations that determines where in the player's path and where on the line the extended player cylinder's surface
		 *   collides with the line segment. To do so, do the following:
		 *    Find the contact point by finding the point relative to the player position that is perpendicular to the line.
		 *    Find the normal of the collision surface by taking a direction perpendicular to the line and horizontal.
		 *    Use cramer's rule to find where the contact point collides with the line.
		 * 
		 * If the solution is outside the height range of the player, check for collision with the player caps using a shear transformation:
		 *    Transform the player with a shear transformation that, when applied to the world, makes the line vertical.
		 *    Check each cap of the player cylinder for collision with the line segment separately.
		 *    If a collision is found, undo the transformation.
		 *    Find the normal of the collision surface by finding the direction perpendicular to the line segment and the tangent to the cylinder cap at the collision point.
		 */
		//Find denominator for Cramer's Rule.
		double denom = -pxd*yd + pyd*xd;
		boolean caps = true; //Flag for checking whether the a collision with the cylinder caps needs to checked.
		
		double t=1, l=0;
		
		if (denom != 0)
		{
			//Check the surface of the cylinder
			caps = false;
			//Find contact point
			double cx = yd, cy = -xd;
			if (pxd*cx + pyd*cy < 0) {cx = -cx; cy = -cy;}
			double dist = Math.sqrt(sqr(cx) + sqr(cy)); cx /= dist; cy /= dist;
			double pxc = cx*radius+px1, pyc = cy*radius+py1;
			
			//Find the player movement distance (t) and where it is on the line (l).
			t = ((pxc-x1)*yd - (pyc-y1)*xd) / denom;
			l = (pxd*(y1-pyc) - pyd*(x1-pxc)) / denom;
			normalX = -cx; normalY = -cy; normalZ = 0;
			
			double zz = pz1 + pzd*t; //Pending player location
			if (zz >= z1 + l*zd || zz+height <= z1 + l*zd) caps = true; //Out of z-range
		}
		
		if (caps) //Cannot collide with cylinder surface
		{
			//A shear transformation will be applied so that the line is vertical but the z-positions are not modified.
			//x_dist and y_dist will be assumed 0 after the transformation.
			double xt = -xd/zd, yt = -yd/zd;
			
			//Transform Player
			double px1_t = px1+pz1*xt, py1_t = py1+pz1*yt;
			double px_dist_t = pxd+pzd*xt, py_dist_t = pyd+pzd*yt;
			double cap_x = height*xt, cap_y = height*yt; //The displacement of the top cap relative to the bottom cap.
			
			//Transform line
			double x1_t = x1+z1*xt, y1_t = y1+z1*yt; //x_dist_t and y_dist_t will always be zero.
			
			//Collide both caps with the line.
			double t1 = getPLineCollisionVertical(px1_t,py1_t,pz1,px_dist_t,py_dist_t,pzd,radius,0, x1_t,y1_t,z1,0,0,zd);
			double nx1 = normalX, ny1 = normalY;
			double t2 = getPLineCollisionVertical(px1_t+cap_x,py1_t+cap_y,pz1+height,px_dist_t,py_dist_t,pzd,radius,0, x1_t,y1_t,z1,0,0,zd);
			double nx2 = normalX, ny2 = normalY;
			
			double nx, ny, nz;
			if (t1 <= t2) {t = t1; nx = nx1; ny = ny1;}
			else          {t = t2; nx = nx2; ny = ny2;}
			
			//Undo transformation
			nz = (-xd*nx - yd*ny)/zd;
			double dist = Math.sqrt(1 + sqr(nz));
			normalX = nx/dist; normalY = ny/dist; normalZ = nz/dist;
			
			if (t<0 || t>=1) return 1;
			return t;
		}
		
		//For surface collisions, return the answer.
		if (t<0 || t>=1 || l<0 || l>1) return 1;
		return t;
	}
	
	//Returns the value from 0 to 1 for the player collision with a horizontal line segment.
	//It does not handle endpoints well. Use another script for that.
	private double getPLineCollisionHorizontal(double px1, double py1, double pz1, double pxd, double pyd, double pzd, double radius, double height,
			double x1, double y1, double z1, double xd, double yd, double zd)
	{
		//The algorithm for this is like the slanted line segment, but simplified.
		
		//Find denominator for Cramer's Rule.
		double denom = -pxd*yd + pyd*xd;
		boolean caps = true;
		
		double t=1, l=0;
		
		if (denom != 0)
		{
			caps = false;
			//Find contact point
			double cx = yd, cy = -xd;
			if (pxd*cx + pyd*cy < 0) {cx = -cx; cy = -cy;}
			double dist = Math.sqrt(sqr(cx) + sqr(cy)); cx /= dist; cy /= dist;
			double pxc = cx*radius+px1, pyc = cy*radius+py1;
			
			//Find the answer and where it is on the line.
			t = ((pxc-x1)*yd - (pyc-y1)*xd) / denom;
			l = (pxd*(y1-pyc) - pyd*(x1-pxc)) / denom;
			normalX = -cx; normalY = -cy; normalZ = 0;
			
			double zz = pz1 + pzd*t; //Pending player location
			if (zz >= z1 || zz+height <= z1) caps = true; //Out of z-range
		}
		
		if (caps) //Cannot collide with cylinder surface
		{
			//Collide cylinder caps
			if (pzd == 0) return 1;
			if (pzd > 0) t = ((z1-height) - pz1) / pzd;
			if (pzd < 0) t = ((z1) - pz1) / pzd;
			normalX = 0; normalY = 0; normalZ = -sign(pzd);
			if (t<0 || t>=1) return 1;
			
			double xx = px1 + pxd*t, yy = py1 + pyd*t; //Pending player location
			
			//Check if in bounds
			if (xd == 0 && yd == 0) return 1;
			double line_length = Math.sqrt(sqr(xd) + sqr(yd));
			if (Math.abs(xx*yd-yy*xd + xd*y1-yd*x1) >= radius*line_length) return 1;
			
			if (xd*(xx-x1) + yd*(yy-y1) < 0) return 1;
			if (xd*(xx-x1-xd) + yd*(yy-y1-yd) > 0) return 1;
			
			return t;
		}
		
		//For surface collisions, return the answer.
		if (t<0 || t>=1 || l<0 || l>1) return 1;
		return t;
	}
	
	//Returns the value from 0 to 1 for the player collision with a vertical line segment.
	//It does not handle endpoints well. Use another script for that.
	private double getPLineCollisionVertical(double px1, double py1, double pz1, double pxd, double pyd, double pzd, double radius, double height,
			double x1, double y1, double z1, double xd, double yd, double zd)
	{
		/*
		 * ALGORITHM 2e:
		 * Reorder the vertices of the line segment as necessary to make the line's direction upwards.
		 * Ignore the z dimension and check how far the player (a circle) must go before it collides with the line (a point).
		 * Test to make sure that the player's z position at the collision is within the necessary bounds to collide with the line.
		 * Find the normal of the collision surface by checking the direction of the circle's center relative to the point.
		 */
		
		//Returns the value from 0 to 1 (see getPlayerCollision) for the player collision with a vertical line segment.
		//It does not handle endpoints well. Use another script for that.
		
		if (zd < 0) {z1 += zd; zd *= -1;} //zd > 0 will hold
		double x_loc = x1-px1, y_loc = y1-py1, loc = sqr(x_loc) + sqr(y_loc); //loc is the location of the line relative to the player ignoring z.
		double pd = sqr(pxd) + sqr(pyd); //pd is the square of the distance that the player wants to travel.
		
		if (pd == 0) return 1;
		
		double d = sqr(pxd*y_loc - pyd*x_loc)/pd; //d is the square of the distance from the player path to the line.
		if (sqr(radius)-d < 0 || loc-d < 0 || pd < 0) return 1; //Line does not intersect with circle.
		
		double t = (Math.sqrt(loc - d) - Math.sqrt(sqr(radius)-d)) / Math.sqrt(pd);
		if (t<0 || t>=1 || pxd*x_loc + pyd*y_loc < 0) return 1; //Out of range of player movement.
		
		double zz = pz1 + pzd*t; //zz is the player's position at the specified t
		if (zz >= z1+zd || zz+height <= z1) return 1; //Out of z-range
		
		normalX = ((px1 + pxd*t)-x1);
		normalY = ((py1 + pyd*t)-y1);
		d = Math.sqrt(sqr(normalX)+sqr(normalY));
		normalX /= d;
		normalY /= d;
		normalZ = 0;
		
		return t;
	}
	
	//Returns the value from 0 to 1 for the player collision with a point.
	private double getPPointCollision(double px1, double py1, double pz1, double pxd, double pyd, double pzd, double radius, double height,
			double x1, double y1, double z1)
	{
		/*
		 * ALGORITHM 2f:
		 * Depending on the player's direction of vertical movement, find where the player will be when it's top/bottom cap is level with the point.
		 * Check whether the point is within this cap. If so, set the normal of the collision surface to <0,0,1> or <0,0,-1> depending on which is appropriate.
		 * 
		 * If the cylinder caps do not collide with the point, check for collision with the surface using a simplified version of algorithm 2e.
		 */
		double t= 1;
		
		//Collide cylinder caps
		if (pzd != 0)
		{
			if (pzd > 0) t = ((z1-height) - pz1) / pzd;
			if (pzd < 0) t = ((z1) - pz1) / pzd;
			normalX = 0; normalY = 0; normalZ = -sign(pzd);
			
			double xx = px1 + pxd*t, yy = py1 + pyd*t; //xx, yy are the player position when moved to be aligned with the point.
			if (t>=0 && t<1 && sqr(xx-x1) + sqr(yy-y1) <= sqr(radius)) return t;
		}
		
		//If the points do not collide with the cylinder's caps, try colliding with the surface instead.
		double x_loc = x1-px1, y_loc = y1-py1, loc = sqr(x_loc) + sqr(y_loc); //loc is the location of the point relative to the player ignoring z.
		double pd = sqr(pxd) + sqr(pyd); //p_dist is the square of the distance that the player wants to travel.
		
		if (pd == 0) return 1;
		
		double d = sqr(pxd*y_loc - pyd*x_loc)/pd; //d is the square of the distance from the player path to the point.
		if (sqr(radius)-d < 0 || loc-d < 0 || pd < 0) return 1; //Line does not intersect with circle.
		
		t = (Math.sqrt(loc - d) - Math.sqrt(sqr(radius)-d)) / Math.sqrt(pd);
		if (t<0 || t>=1 || pxd*x_loc + pyd*y_loc < 0) return 1; //Out of range of player movement.
		
		double zz = pz1 + pzd*t; //zz is the player's position at the specified t
		if (zz >= z1 || zz+height <= z1) return 1; //Out of z-range
		
		normalX = ((px1 + pxd*t)-x1);
		normalY = ((py1 + pyd*t)-y1);
		d = Math.sqrt(sqr(normalX)+sqr(normalY));
		normalX /= d;
		normalY /= d;
		normalZ = 0;
		return t;
	}
	
	//Returns the minimum of the three arguments.
	private double min3(double a, double b, double c)
	{
		if (a < b && a < c) return a;
		else if (b < c) return b;
		return c;
	}
	
	//Returns the maximum of the three arguments.
	private double max3(double a, double b, double c)
	{
		if (a > b && a > c) return a;
		else if (b > c) return b;
		return c;
	}
	
	//Returns 1 if x is positive, -1 if x is negative, and 0 if x is 0.
	private int sign(double x)
	{
		if (x > 0) return 1;
		if (x < 0) return -1;
		return 0;
	}
	
	//Returns x^2
	private double sqr(double x)
	{
		return x*x;
	}
}
