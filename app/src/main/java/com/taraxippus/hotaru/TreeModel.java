package com.taraxippus.hotaru;

import com.taraxippus.yui.model.IcoSphereModel;
import com.taraxippus.yui.model.Model;
import com.taraxippus.yui.render.Pass;
import com.taraxippus.yui.util.Quaternion;
import com.taraxippus.yui.util.TriangleIndices;
import com.taraxippus.yui.util.VectorF;
import java.util.ArrayList;
import java.util.Random;
import java.lang.reflect.GenericDeclaration;

public class TreeModel extends Model
{
	private static IcoSphereModel leavesBaseMain, leavesBaseSmall;
	
	final Random random;
	
	ArrayList<VectorF> vertices;
	ArrayList<TriangleIndices> faces;
	
	public TreeModel(Pass pass, Random random)
	{
		super(pass);
		
		if (leavesBaseMain == null)
		{
			leavesBaseMain = new IcoSphereModel(pass, 1);
			leavesBaseMain.generate();
			
			leavesBaseSmall = new IcoSphereModel(pass, 0);
			leavesBaseSmall.generate();
		}
		
		this.random = random;
	}
	
	public void generate()
	{
		vertices = new ArrayList<VectorF>();
		faces = new ArrayList<TriangleIndices>();
		
		VectorF center = VectorF.obtain().set(0, 0, 0);
		VectorF tmp = VectorF.obtain();
		Quaternion rotation = Quaternion.obtain();
		rotation.rotateY((float) Math.PI * 2 * random.nextFloat());
		rotation.rotateX((float) Math.PI / 32F * random.nextFloat());
		Quaternion up = Quaternion.obtain().set(rotation);
		
		int trunkRings = 4 + random.nextInt(4);
		int trunkSectors = 3 + random.nextInt(4);
		float width, minWidth = (0.5F + random.nextFloat() * 0.25F) * (width = 0.075F + 0.05F * random.nextFloat());
		float height = trunkRings * width * 3F;

		ArrayList<Branch> branches = new ArrayList<Branch>();
		
		int dirChange = random.nextInt(1);
		int x, y;
		for (y = 0; y <= trunkRings; ++y)
		{
			rotation.normalize();
			for (x = 0; x < trunkSectors; ++x)
			{
				tmp.set(width * (float) Math.sin(Math.PI * 2 * x / trunkSectors), 0, width * (float) Math.cos(Math.PI * 2 * x / trunkSectors));
				if (y == 0)
					tmp.multiplyBy(1F + random.nextFloat() * 0.5F, 1, 1 + random.nextFloat() * 0.5F);
				else
					tmp.multiplyBy(0.8F + random.nextFloat() * 0.3F);

				tmp.rotate(rotation).add(center);
				vertices.add(VectorF.obtain().set(tmp));
				
				if (y < trunkRings && x < trunkSectors - 1)
				{
					faces.add(new TriangleIndices(y * trunkSectors + x, y * trunkSectors + x + 1, (y + 1) * trunkSectors + x));
					faces.add(new TriangleIndices(y * trunkSectors + x + 1, (y + 1) * trunkSectors + x + 1, (y + 1) * trunkSectors + x));
				}
			}
			
			if (y < trunkRings)
			{
				width -= minWidth / trunkRings * (1 + random.nextFloat() * 0.25F);
				tmp.set(0, y == 0 ? 0.1F : height / trunkRings, 0).rotate(rotation);
				center.add(tmp);

				if (y > 0 && random.nextInt(4) == 0)
				{
					dirChange++;
					float rotationX = (dirChange % 2 == 1 ? -1 : 1) * (float) Math.PI / 8F * random.nextFloat();
					rotation.rotateX(rotationX);
					
					if (random.nextInt(3) != 0)
						branches.add(new Branch(VectorF.obtain().set(center), Quaternion.obtain().set(rotation).rotateX((dirChange % 2 == 1 ? 1 : -1) * (float) Math.PI / 8F * (0.5F + random.nextFloat()) - rotationX * 2), up, width, (trunkRings - y), (y + 1) * trunkSectors));
				}
				else if (y > 0 && random.nextInt(2) == 0)
					rotation.rotateY((float) -Math.PI / 32 * random.nextFloat());
				
				faces.add(new TriangleIndices(y * trunkSectors + x - 1, y * trunkSectors, (y + 1) * trunkSectors + x - 1));
				faces.add(new TriangleIndices(y * trunkSectors, (y + 1) * trunkSectors, (y + 1) * trunkSectors + x - 1));
			}
		}
		
		for (Branch b : branches)
			b.generate();
			
		generateLeaves(center, true);

		tmp.release();
		center.release();
		rotation.release();
		up.release();
	}
	
	public void generateLeaves(VectorF offset, boolean main)
	{
		int index = vertices.size();
		ArrayList<VectorF> leafVertices;
		float scale = 0.8F + random.nextFloat() * 0.4F;
		
		if (main)
		{
			leafVertices = VectorF.copyList(leavesBaseMain.vertices);
			TriangleIndices.copyToList(faces, leavesBaseMain.faces, index);
			
			for (VectorF v : leafVertices)
				v.multiplyBy(0.8F + random.nextFloat() * 0.4F, 0.4F + random.nextFloat() * 0.2F, 0.8F + random.nextFloat() * 0.4F).multiplyBy(scale).add(offset);
		}
		else
		{
			leafVertices = VectorF.copyList(leavesBaseSmall.vertices);
			TriangleIndices.copyToList(faces, leavesBaseSmall.faces, index);
			
			for (VectorF v : leafVertices)
				v.multiplyBy(0.4F + random.nextFloat() * 0.2F, 0.2F + random.nextFloat() * 0.1F, 0.4F + random.nextFloat() * 0.2F).multiplyBy(scale).add(offset);
		}
		
		vertices.addAll(leafVertices);
	}
	
	@Override
	public float[] getVertices()
	{
		generate();
		
		return VectorF.toArrayList(vertices, true);
	}

	@Override
	public short[] getIndices()
	{
		return TriangleIndices.toIndices(faces, 0);
	}
	
	@Override
	protected void freeShapeSpace()
	{
		super.freeShapeSpace();

		vertices = null;
		faces = null;
	}
	
	public class Branch
	{
		final VectorF offset;
		final Quaternion direction, up;
		float width;
		int rings;
		final short trunk;
		public Branch(VectorF offset, Quaternion direction, Quaternion up, float width, int rings, int trunk)
		{
			this.offset = offset;
			this.direction = direction;
			this.up = up;
			this.width = width;
			this.rings = rings;
			this.trunk = (short) trunk;
		}
		
		public void generate()
		{
			int faceOffset = vertices.size();
			float length = width * rings * 1.25F;
			float minWidth = (0.5F + random.nextFloat() * 0.25F) * width;
			final VectorF tmp = VectorF.obtain();
			Quaternion rotation = Quaternion.obtain();
			
			int x, y;
			for (y = 0; y <= rings; ++y)
			{
				rotation.slerp(direction, up, (float) y / rings).normalize();
				width -= minWidth / rings * (1 + random.nextFloat() * 0.25F);
				tmp.set(0, y == 0 ? 0.1F : length / rings, 0).rotate(rotation);
				offset.add(tmp);
				
				for (x = 0; x < 4; ++x)
				{
					tmp.set(width * (float) Math.sin(Math.PI * 2 * x / 4), 0, width * (float) Math.cos(Math.PI * 2 * x / 4));
					tmp.multiplyBy(0.8F + random.nextFloat() * 0.3F);

					tmp.rotate(rotation).add(offset);
					vertices.add(VectorF.obtain().set(tmp));

					if (y < rings && x < 4 - 1)
					{
						faces.add(new TriangleIndices(faceOffset + y * 4 + x, faceOffset + y * 4 + x + 1, faceOffset + (y + 1) * 4 + x));
						faces.add(new TriangleIndices(faceOffset + y * 4 + x + 1, faceOffset + (y + 1) * 4 + x + 1, faceOffset + (y + 1) * 4 + x));
					}
				}

				if (y < rings)
				{
					faces.add(new TriangleIndices(faceOffset + y * 4 + x - 1, faceOffset + y * 4, faceOffset + (y + 1) * 4 + x - 1));
					faces.add(new TriangleIndices(faceOffset + y * 4, faceOffset + (y + 1) * 4, faceOffset + (y + 1) * 4 + x - 1));
				}
			}
			
			faces.add(new TriangleIndices(trunk, trunk + 1, faceOffset));
			faces.add(new TriangleIndices(trunk + 1, faceOffset + 1, faceOffset));
			faces.add(new TriangleIndices(trunk + 1, trunk + 2, faceOffset + 1));
			faces.add(new TriangleIndices(trunk + 2, faceOffset + 1, faceOffset + 1));
			faces.add(new TriangleIndices(trunk + 2, trunk + 3, faceOffset + 2));
			faces.add(new TriangleIndices(trunk + 3, faceOffset + 3, faceOffset + 2));
			faces.add(new TriangleIndices(trunk + 3, trunk, faceOffset + 3));
			faces.add(new TriangleIndices(trunk, faceOffset, faceOffset + 3));
			
			generateLeaves(offset, false);
			
			tmp.release();
			rotation.release();
			offset.release();
			direction.release();
			up.release();
		}
	}
}
