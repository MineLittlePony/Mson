// import { Mson } from './mson_loader';
// import { objUtils } from './obj_utils';
//=====================================================//
//                 The base types
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
  function present(a) {
    return !!a;
  }

  Mson.addElementType('mson:box', (loader, body, locals, model) => {
    return {
      from: locals.array(fixedLength(body.from, 3)),
      size: locals.array(fixedLength(body.size, 3)),
      texture: loader.getTexture(body.texture || [], model.texture),
      stretch: fixedLength(body.stretch, 3),
      mirror: body.mirror
    };
  }, (parent, context) => {
    // TODO: rendering
  });
  Mson.addElementType('mson:compound', (loader, body, locals, model, defineName) => {
    const element = {
      center: locals.array(fixedLength(body.center, 3)),
      offset: locals.array(fixedLength(body.offset, 3)),
      rotate: locals.array(fixedLength(body.rotate, 3)),
      mirror: fixedLength(body.mirror, 3, false),
      visible: body.visible === true,
      texture: locals.obj(loader.getTexture(body.texture, model.texture)),
      children: loadChildren(body.children),
      cubes: body.cubes ? body.cubes
          .map(cube => loader.getElement(child, 'mson:box', model, defineName))
          .filter(present) : []
    };
    if (body.name) {
      defineName(body.name, element);
    }
    function loadChildren(children) {
      return children ? objUtils.map(children,
        value => loader.getElement(value, 'mson:compound', model, defineName),
        Array.isArray(children) ? (key => `unnamed_member_${key}`) : (key => key)
      ) : {};
    }

    return element;
  }, (parent, context) => {
    if (!this.visible) {
      return;
    }

    // TODO: rendering

    Object.values(this.children).forEach(child => child.render(this, context));
    this.cubes.forEach(cube => cube.render(this, context));
  });
})();