package com.taraxippus.hotaru;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import com.taraxippus.yui.Main;
import com.taraxippus.yui.game.Game;
import com.taraxippus.yui.game.OutlineSceneObject;
import com.taraxippus.yui.game.SceneObject;
import com.taraxippus.yui.game.TexturedSceneObject;
import com.taraxippus.yui.model.Model;
import com.taraxippus.yui.model.UVBoxModel;
import com.taraxippus.yui.render.ConfigChooser;
import com.taraxippus.yui.render.Pass;
import com.taraxippus.yui.texture.NoiseTexture;
import com.taraxippus.yui.texture.ProceduralTexture;
import com.taraxippus.yui.util.VectorF;
import java.util.Random;
import com.taraxippus.yui.game.ParticleEmitter;
import com.taraxippus.yui.model.BoxModel;

public class MainActivity extends Main
{
	public static Pass PASS_SCENE, PASS_SCENE_OUTLINE, PASS_SCENE_TEXTURE, PASS_PARTICLE;
	public static Pass.DefaultPost PASS_POST;
	public static Pass.Bloom PASS_BLOOM1, PASS_BLOOM2, PASS_BLOOM3, PASS_BLOOM4, PASS_BLOOM5, PASS_BLOOM6;
	public static final Pass[] passes = new Pass[9];
	
	public static final float FOG = 0.05F;
	
	@Override
	public Game createGame() { return new MainGame(this); }

	@Override
	public void initPasses()
	{
		int pass = 0;
		passes[pass++] = PASS_SCENE = new Pass(this, com.taraxippus.yui.R.raw.vertex_scene, com.taraxippus.yui.R.raw.fragment_scene, new String[] { "a_Position" },  new int[] { 3 }, new String[] { "u_MVP", "u_MV" });
		GLES20.glUniform4f(PASS_SCENE.getProgram().getUniform("u_Fog"), 0.25F, 0.25F, 0.25F, FOG);
	
		passes[pass++] = PASS_PARTICLE = new Pass.DefaultParticle(this);
		GLES20.glUniform4f(PASS_PARTICLE.getProgram().getUniform("u_Fog"), 0.25F, 0.25F, 0.25F, FOG * 0.25F);
		
		passes[pass++] = PASS_BLOOM1 = new Pass.Bloom(this, renderer.width / 2, renderer.height / 2, 1, true, 0.75F, 0.125F, 0.125F, -0.7F);
		passes[pass++] = PASS_BLOOM2 = new Pass.Bloom(this, renderer.width / 2, renderer.height / 2, 2, false);
		passes[pass++] = PASS_BLOOM3 = new Pass.Bloom(this, renderer.width / 4, renderer.height / 4, 1, true);
		passes[pass++] = PASS_BLOOM4 = new Pass.Bloom(this, renderer.width / 4, renderer.height / 4, 2, false);
		passes[pass++] = PASS_BLOOM5 = new Pass.Bloom(this, renderer.width / 8, renderer.height / 8, 1, true);
		passes[pass++] = PASS_BLOOM6 = new Pass.Bloom(this, renderer.width / 8, renderer.height / 8, 2, false);
		
		passes[pass++] = PASS_POST = new Pass.DefaultPost(this, renderer.width, renderer.height, 0.5F, 0.2F, new int[] { PASS_BLOOM2.getFramebufferTexUnit(), PASS_BLOOM4.getFramebufferTexUnit(), PASS_BLOOM6.getFramebufferTexUnit() });
	
		PASS_BLOOM1.setInputTexture(PASS_POST.getFramebufferTexUnit());
		PASS_BLOOM2.setInputTexture(PASS_BLOOM1.getFramebufferTexUnit());
		PASS_BLOOM3.setInputTexture(PASS_BLOOM2.getFramebufferTexUnit());
		PASS_BLOOM4.setInputTexture(PASS_BLOOM3.getFramebufferTexUnit());
		PASS_BLOOM5.setInputTexture(PASS_BLOOM4.getFramebufferTexUnit());
		PASS_BLOOM6.setInputTexture(PASS_BLOOM5.getFramebufferTexUnit());
	}

	@Override
	public Pass[] getPasses() { return passes; }

	@Override
	public Pass getDefaultPass() { return PASS_SCENE; }

	@Override
	public Pass getDefaultParticlePass() { return PASS_PARTICLE; }

	@Override
	public GLSurfaceView.EGLConfigChooser getConfigChooser() { return new ConfigChooser(this, false, false); }
	
	@Override
	public VectorF getClearColor() { return VectorF.obtain().set(0.25F, 0.25F, 0.25F).release(); }

	@Override
	public Pass getPostPass() { return PASS_POST; }
	
	public class MainGame extends Game
	{
		final ProceduralTexture texture = new NoiseTexture(new Random(), 16, 16, 254, 255);
		
		public MainGame(Main main) { super(main); }
		
		@Override
		public void init()
		{
			this.setUseDefaultCamera(true, false);
			camera.rotation.set(-45, 0, 0);
			camera.position.set(0, 1, -1);
			camera.updateView();
			
			world.add(new SceneObject(world, new BoxModel(PASS_SCENE)).scale(5F, 0.1F, 5F).setColor(0x666666));
			
			texture.init();		
			ParticleEmitter pe = new ParticleEmitter(world, 150);
			pe.color_start_min.set(1.2F, 0.5F, 0.1F);
			pe.color_start_max.set(1.0F, 0.8F, 0.15F);
			pe.color_end_min.set(1.0F, 0.5F, 0.05F);
			pe.color_end_max.set(2.0F, 0.25F, 0.025F);
			pe.acceleration = 0.99F;
			pe.minAlphaStart = 1F;
			pe.maxAlphaStart = 1F;
			pe.minAlphaEnd = 1.0F;
			pe.maxAlphaEnd = 0.75F;
			pe.minLifetime = 2F;
			pe.maxLifetime = 3F;
			pe.maxMinSize = 0;
			pe.minMinSize = 0;
			pe.minMaxSize = 0.01F;
			pe.setTexture(texture);
			pe.renderAlways = true;
			pe.translate(0, 0.5F, 0);
			world.add(pe);
		}

		@Override
		public void delete()
		{
			super.delete();
			
			texture.delete();
		}

		@Override
		public void onTap(MotionEvent e)
		{
			
		}
	}
}
