package com.taraxippus.hotaru;

import android.opengl.GLES20;
import com.taraxippus.yui.Main;
import com.taraxippus.yui.game.Game;
import com.taraxippus.yui.game.OutlineSceneObject;
import com.taraxippus.yui.game.SceneObject;
import com.taraxippus.yui.model.Model;
import com.taraxippus.yui.render.Pass;
import com.taraxippus.yui.util.PoissonDisk;
import com.taraxippus.yui.util.SimplexNoise;
import com.taraxippus.yui.util.VectorF;
import java.util.ArrayList;

public class MainActivity extends Main
{
	public static Pass PASS_SCENE;
	public static Pass.Post PASS_POST;
	public static Pass.Bloom PASS_BLOOM1, PASS_BLOOM2;
	public static final Pass[] passes = new Pass[2];
	
	@Override
	public Game createGame() { return new MainGame(this); }

	@Override
	public void initPasses()
	{
		//passes[0] = PASS_SCENE = new Pass(this, com.taraxippus.yui.R.raw.vertex_scene_normal, com.taraxippus.yui.R.raw.fragment_scene_normal, new String[] { "a_Position", "a_Normal" },  new int[] { 3, 3 }, new String[] { "u_MVP", "u_N" });
		//passes[0] = PASS_SCENE = new Pass(this, com.taraxippus.yui.R.raw.vertex_scene, com.taraxippus.yui.R.raw.fragment_scene, new String[] { "a_Position" },  new int[] { 3 }, new String[] { "u_MVP" });
		
		passes[0] = PASS_SCENE = new Pass(this, com.taraxippus.yui.R.raw.vertex_scene_normal, com.taraxippus.yui.R.raw.fragment_scene_light, new String[] { "a_Position", "a_Normal" },  new int[] { 3, 3 }, new String[] { "u_MVP", "u_N" });
		GLES20.glUniform4f(PASS_SCENE.getProgram().getUniform("u_Fog"), 0.0F, 0.25F, 1F, 0.01F);
		final VectorF light = new VectorF().set(0, -1, -0.5F).normalize().release();
		GLES20.glUniform4f(PASS_SCENE.getProgram().getUniform("u_Light"), light.x, light.y, light.z, 0.9F);
		
		//passes[1] = PASS_BLOOM1 = new Pass.Bloom(this, renderer.width, renderer.height, true, 0, 0, 0.75F, -0.25F);
		//passes[2] = PASS_BLOOM2 = new Pass.Bloom(this, renderer.width, renderer.height, false);
		
		passes[1] = PASS_POST = new Pass.DefaultPost(this, renderer.width, renderer.height, 0.8F, 0.2F, new int[] { /*"PASS_BLOOM2.getFramebufferTexUnit()*/ });
	
		//PASS_BLOOM1.setInputTexture(PASS_POST.getFramebufferTexUnit());
		//PASS_BLOOM2.setInputTexture(PASS_BLOOM1.getFramebufferTexUnit());
	}

	@Override
	public Pass[] getPasses() { return passes; }

	@Override
	public Pass getDefaultPass() { return PASS_SCENE; }

	@Override
	public Pass getDefaultParticlePass() { return null; }

	@Override
	public VectorF getClearColor() { return VectorF.obtain().set(0, 0.5F, 1).release(); }

	@Override
	public Pass getPostPass() { return PASS_POST; }
	
	public class MainGame extends Game
	{
		public MainGame(Main main) { super(main); }
		
		@Override
		public void init()
		{
			this.setUseDefaultCamera(true);
			
			final ArrayList<VectorF> positions = PoissonDisk.randomDistribution(world.random, 10, 10, 0.5F, 30);
			final SimplexNoise noise = new SimplexNoise(256, 0.5F, world.random.nextLong());
			
			for (VectorF v : positions)
			{
				final Model treeModel = new TreeModel(PASS_SCENE, world.random);

				SceneObject tree = new SceneObject(world, treeModel);
				tree.translate(v.x, noise.getNoise(v.x * 7, v.z * 7) * 0.5F, v.z);
				world.add(tree);
				
				v.release();
			}
			
			camera.position.add(0, 1, - 3);
			//camera.setTarget(box);
		}
	}
}
