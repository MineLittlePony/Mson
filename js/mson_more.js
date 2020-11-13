// import { Mson } from './mson_loader';
//=====================================================//
//                 Mson specific
(_ => {
  function fixedLength(arr, len, fillWith) {
    if (fillWith === undefined) {
      fillWith = 0;
    }
    while (arr.length < len) {
      arr.push(fillWith);
    }
    return arr;
  }

  Mson.addElementType('mson:plane', (loader, body, locals, model) => {
    return {
      position: locals.array(fixedLength(body.position, 3)),
      size: locals.array(fixedLength(body.size, 3)),
      texture: locals.obj(loader.getTexture(body.texture, model.texture)),
      mirror: locals.array(fixedLength(body.mirror, 3, false)),
      stretch: locals.array(fixedLength(body.stretch, 3)),
      face: body.face
    };
  }, (parent, context) => {
    // TODO: rendering
  });
  Mson.addElementType('mson:planar', (loader, body, locals, model) => {
    const directions = 'up;down;west;east;north;sound'.split(';');

    const faces = [];
    directions.forEach(face => {
      if (body[face] && body[face].length) {
        let set = body[face][0].length ? body[face] : [ body[face] ];
        faces.push(set.map(i => new Face(i)));
      }
    });

    function Face(element) {
      this.position = [ element[0], element[1], element[2] ];
      this.size = [ element[3], element[4] ];
      if (element.length > 6) {
        this.texture = loader.getTexture(element.length > 6 ? [
          locals.get(element[5]),
          locals.get(element[6])
        ] : [], model.texture);
      }
    }

    return {
      stretch: locals.array(fixedLength(body.stretch, 3)),
      faces
    };
  }, (parent, context) => {
    const stretch = this.stretch(locals);

    this.faces.forEach(set => {
      set.forEach(plane => {
        // TODO: rendering
      });
    });
  });
  Mson.addElementType('mson:cone', (loader, body, locals, model) => {
    return {
      from: locals.array(fixedLength(body.from, 3)),
      size: locals.array(fixedLength(body.size, 3)),
      texture: locals.obj(loader.getTexture(body.texture, model.texture)),
      stretch: fixedLength(body.stretch, 3),
      mirror: body.mirror,
      taper: body.taper
    };
  }, (parent, context) => {
    // TODO: rendering
  });
  Mson.addElementType('mson:quads', (loader, body, locals, model) => {
    const vertices = body.vertices.map(vert => Vertex(vert));
    const quads = body.quads.map(quad => {
      return quad.vertices.map(index => vertices[index]);
    });

    function Vertex(body) {
      if (body.length) {
        return {
          x: body[0] || 0,
          y: body[1] || 0,
          z: body[2] || 0,
          u: body[3] || 0,
          v: body[4] || 0
        };
      }
      return Mson.objUtils.copy(Vertex([]), body);
    }

    return {
      quads,
      u: body.u || 0,
      v: body.v || 0
    };
  }, (parent, context) => {

    this.quads.forEach(quad => {
      quad.forEach(vertex => {
        // TODO: rendering
      });
    });
  });
})();
