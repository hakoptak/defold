#ifndef DEBUGRENDERER_H
#define DEBUGRENDERER_H_

#include <vectormath/cpp/vectormath_aos.h>
#include <graphics/graphics_device.h>


using namespace Vectormath::Aos;

namespace dmRenderDebug
{

    /**
     * Initialize debug render system
     * @param renderworld Global renderworld to add entities to
     */
    void Initialize(dmRender::HRenderWorld renderworld);

    /**
     * Finalize debug render system
     */
    void Finalize();

    /**
     * Update debug render system. Should be used to send a heart-beat every frame
     */
    void Update();

    /**
     * SetFragmentProgram Assign a fragment program to renderer
     * @param program Fragment program
     */
    void SetFragmentProgram(dmGraphics::HFragmentProgram program);

    /**
     * SetVertexProgram Assign a vertex program to renderer
     * @param program Vertex program
     */
    void SetVertexProgram(dmGraphics::HVertexProgram program);

    /**
     * Square Render debug square (2D)
     * @param position Position
     * @param size Size
     * @param color Color
     */
    void Square(Point3 position, Vector3 size, Vector4 color);

    /**
     * Cube Render debug cube (3D)
     * @param position Position
     * @param size Size
     */
    void Cube(Point3 position, float size);

    /**
     * Line2D Render debug line
     * @param start Start point
     * @param end End point
     * @param color Color
     */
    void Line2D(Point3 start, Point3 end, Vector4 color);

    /**
     * Line3D Render debug line
     * @param start Start point
     * @param end End point
     * @param color Color
     */
    void Line3D(Point3 start, Point3 end, Vector4 color);

    /**
     * Lines2D Render debug lines (line strip)
     * @param vertices Points
     * @param vertex_count Number of points (>1)
     * @param color Color
     */
    void Lines2D(Point3* vertices, uint32_t vertex_count, Vector4 color);

    /**
     * Lines3D Render debug lines (line strip)
     * @param vertices Points
     * @param vertex_count Number of points (>1)
     * @param color Color
     */
    void Lines3D(Point3* vertices, uint32_t vertex_count, Vector4 color);

}

#endif // DEBUGRENDERER_H
