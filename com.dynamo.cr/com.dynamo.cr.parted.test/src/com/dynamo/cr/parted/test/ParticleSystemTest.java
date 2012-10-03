package com.dynamo.cr.parted.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.eclipse.swt.widgets.Display;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dynamo.cr.parted.ParticleLibrary;
import com.dynamo.cr.parted.ParticleLibrary.AnimationData;
import com.dynamo.cr.parted.ParticleLibrary.FetchAnimationCallback;
import com.dynamo.cr.parted.ParticleLibrary.Quat;
import com.dynamo.cr.parted.ParticleLibrary.RenderInstanceCallback;
import com.dynamo.cr.parted.ParticleLibrary.Vector3;
import com.dynamo.particle.proto.Particle.EmissionSpace;
import com.dynamo.particle.proto.Particle.Emitter;
import com.dynamo.particle.proto.Particle.EmitterKey;
import com.dynamo.particle.proto.Particle.EmitterType;
import com.dynamo.particle.proto.Particle.ParticleFX;
import com.dynamo.particle.proto.Particle.PlayMode;
import com.dynamo.particle.proto.Particle.SplinePoint;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.opengl.util.BufferUtil;

public class ParticleSystemTest {

    private Pointer context;

    private static final int MAX_PARTICLE_COUNT = 1024;

    @Before
    public void setUp() throws Exception {
        // Avoid hang when running unit-test on Mac OSX
        // Related to SWT and threads?
        if (System.getProperty("os.name").toLowerCase().indexOf("mac") != -1) {
            Display.getDefault();
        }

        context = ParticleLibrary.Particle_CreateContext(32, MAX_PARTICLE_COUNT);
    }

    @After
    public void tearDown() {
        ParticleLibrary.Particle_DestroyContext(context);
    }

    @Test
    public void smokeTest() throws IOException {
        Emitter.Property.Builder pb = Emitter.Property.newBuilder()
                .setKey(EmitterKey.EMITTER_KEY_PARTICLE_LIFE_TIME)
                .addPoints(SplinePoint.newBuilder()
                        .setX(0.0f)
                        .setY(1.0f)
                        .setTX(1.0f)
                        .setTY(0.0f))
                .addPoints(SplinePoint.newBuilder()
                        .setX(1.0f)
                        .setY(1.0f)
                        .setTX(1.0f)
                        .setTY(0.0f));
        Emitter.Builder eb = Emitter.newBuilder()
                .setMode(PlayMode.PLAY_MODE_ONCE)
                .setSpace(EmissionSpace.EMISSION_SPACE_WORLD)
                .setPosition(com.dynamo.proto.DdfMath.Point3.newBuilder().build())
                .setRotation(com.dynamo.proto.DdfMath.Quat.newBuilder().build())
                .setTileSource("foo")
                .setAnimation("anim")
                .setMaterial("test")
                .setMaxParticleCount(1)
                .setDuration(1.0f)
                .setType(EmitterType.EMITTER_TYPE_SPHERE)
                .addProperties(pb);
        ParticleFX.Builder pfxb = ParticleFX.newBuilder()
                .addEmitters(eb);

        byte[] pfxData = pfxb.build().toByteArray();

        Pointer prototype = ParticleLibrary.Particle_NewPrototype(ByteBuffer.wrap(pfxData), pfxData.length);
        Pointer instance = ParticleLibrary.Particle_CreateInstance(context, prototype);
        assertNotNull(instance);
        ParticleLibrary.Particle_SetPosition(context, instance, new Vector3(1, 2, 3));
        ParticleLibrary.Particle_SetRotation(context, instance, new Quat(0, 0, 0, 1));

        ParticleLibrary.Particle_StartInstance(context, instance);

        final Pointer originalMaterial = new Pointer(1);
        ParticleLibrary.Particle_SetMaterial(prototype, 0, originalMaterial);
        final Pointer originalTileSource = new Pointer(2);
        ParticleLibrary.Particle_SetTileSource(prototype, 0, originalTileSource);
        final Pointer originalTexture = new Pointer(3);
        final FloatBuffer texCoords = BufferUtil.newFloatBuffer(4);
        texCoords.put(1.0f).put(2.0f).put(3.0f).put(4.0f).flip();
        IntByReference outSize = new IntByReference(1234);
        // 6 vertices * 6 floats
        final int elementCount = MAX_PARTICLE_COUNT * 6 * 6;
        final int vertexBufferSize = elementCount * 4;
        final FloatBuffer vertexBuffer = BufferUtil.newFloatBuffer(elementCount);
        final boolean fetchAnim[] = new boolean[] { false };
        ParticleLibrary.Particle_Update(context, 1.0f / 60.0f, vertexBuffer, vertexBufferSize, outSize,
                new FetchAnimationCallback() {

                    @Override
                    public int invoke(Pointer tileSource, long hash, AnimationData data) {
                        assertTrue(tileSource.equals(originalTileSource));
                        long h = ParticleLibrary.Particle_Hash("anim");
                        assertTrue(hash == h);
                        data.texCoords = texCoords;
                        data.texture = originalTexture;
                        data.playback = ParticleLibrary.AnimPlayback.ANIM_PLAYBACK_ONCE_FORWARD;
                        data.startTile = 1;
                        data.endTile = 1;
                        data.fps = 30;
                        data.hFlip = 0;
                        data.vFlip = 0;
                        fetchAnim[0] = true;
                        data.structSize = data.size();
                        return ParticleLibrary.FetchAnimationResult.FETCH_ANIMATION_OK;
                    }
                });
        assertTrue(fetchAnim[0]);
        int vertexSize = outSize.getValue();
        assertTrue(6 * 6 * 4 == vertexSize);
        int uvIdx[] = new int[] {
                0, 3,
                0, 1,
                2, 3,
                2, 3,
                0, 1,
                2, 1
        };
        for (int i = 0; i < 6; ++i) {
            // u
            assertTrue(texCoords.get(uvIdx[i * 2 + 0]) == vertexBuffer.get());
            // v
            assertTrue(texCoords.get(uvIdx[i * 2 + 1]) == vertexBuffer.get());
            // p
            assertTrue(1.0f == vertexBuffer.get());
            assertTrue(2.0f == vertexBuffer.get());
            assertTrue(3.0f == vertexBuffer.get());
            // a
            assertTrue(0.0f == vertexBuffer.get());
        }

        final boolean rendered[] = new boolean[] { false };
        ParticleLibrary.Particle_Render(context, new Pointer(1122), new RenderInstanceCallback() {
            @Override
            public void invoke(Pointer userContext, Pointer material,
                    Pointer texture, int vertexIndex, int vertexCount) {
                assertTrue(material.equals(originalMaterial));
                assertTrue(texture.equals(originalTexture));
                assertEquals(new Pointer(1122), userContext);
                rendered[0] = true;
            }
        });
        assertTrue(rendered[0]);

        ParticleLibrary.Particle_StopInstance(context, instance);
        ParticleLibrary.Particle_RestartInstance(context, instance);
        ParticleLibrary.Particle_DestroyInstance(context, instance);
        ParticleLibrary.Particle_DeletePrototype(prototype);
    }

}

