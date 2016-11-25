package com.taraxippus.hotaru;

import android.os.Bundle;
import android.util.Log;
import com.taraxippus.yui.Main;
import com.taraxippus.yui.game.Game;
import com.taraxippus.yui.game.SceneObject;
import com.taraxippus.yui.model.BoxModel;
import com.taraxippus.yui.model.Model;
import com.taraxippus.yui.render.Pass;
import android.widget.TextView;
import com.taraxippus.yui.util.VectorF;
import com.taraxippus.yui.model.SphereModel;

public class MainActivity extends Main
{
	public static Pass PASS_SCENE, PASS_BLOOM;
	public static final Pass[] passes = new Pass[4];
	
	@Override
	public Game createGame() { return new MainGame(this); }

	@Override
	public void initPasses()
	{
		passes[0] = PASS_SCENE = new Pass(this, com.taraxippus.yui.R.raw.vertex_scene, com.taraxippus.yui.R.raw.fragment_scene, new String[] { "a_Position" },  new int[] { 3 }, new String[] { "u_MVP" });
		passes[1] = PASS_BLOOM = new Pass.Bloom();
		passes[2] = new Pass.Bloom();
		passes[3] = new Pass.DefaultPost(this, renderer.width, renderer.height, 0.8F, 0.2F, new int[0]);
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
	public Pass getFirstPostPass() { return PASS_BLOOM; }
	
	public class MainGame extends Game
	{
		public MainGame(Main main) { super(main); }
		
		@Override
		public void init()
		{
			final Model boxModel = new SphereModel(PASS_SCENE, 30, 30);
			
			SceneObject box = new SceneObject(world, boxModel);
			box.scale(0.25F, 0.25F, 0.25F);
			box.setColor(0xFF8800);
			world.add(box);
			
			camera.position.add(0, 1, - 3);
			//camera.setTarget(box);
		}
	}
}
