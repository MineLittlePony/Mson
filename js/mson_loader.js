// import { Incomplete } from './incomplete';
/**
 * The core of Mson's model loading functionality.
 *
 * Usage:
 *
 * const loader = Mson.createLoader();
 * loader.addFile("skeleton", "<json data>");
 * loader.addFile("pony", "<json data>");
 *
 * const skeletonModel = loader.getModel("skeleton");
 *
 * skeletonModel.render();
 */
/*export*/ const Mson = (_ => {
  const reservedKeys = 'parent;locals;texture;scale'.split(';');
  const defaultTexture = Texture([0, 0, 64, 32]);

  const elementTypes = {};

  const objUtils = {
    map(obj, valueMapper) {
      const result = {};
      Object.keys(obj).forEach(key => result[key] = valueMapper(obj[key]));
      return result;
    },
    copy(to, from) {
      Object.keys(from).forEach(key => to[key] = from[key]);
      return to;
    },
    subset(obj, keys) {
      const result = {};
      keys.forEach(key => result[key] = obj[key]);
      return result;
    }
  };

  function createLoader() {
    const files = {};

    function Texture(body, parent) {
      body = body || {};
      parent = parent || {};
      if (body.map) {
        return {
          u: body[0] || parent.u || 0,
          v: body[1] || parent.v || 0,
          w: body[2] || parent.w || 0,
          h: body[3] || parent.h || 0
        };
      }
      return objUtils.copy(objUtils.copy({}, parent), body);
    }

    function File(body) {
      const parameters = body.substring ? JSON.parse(body) : body;

      function loadElements(loader, model) {
        const locals = model.locals;
        const incoming = {};
        const elementNames = Object.keys(parameters).filter(key => reservedKeys.indexOf(key) == -1);
        const elements = objUtils.map(objUtils.subset(parameters, elementNames), element -> {
          return loader.getElement(element, 'mson:compound', model, {
            get(input) {
              return Incomplete.of(input)(locals);
            },
            array(input) {
              return Incomplete.array(input)(locals);
            },
            obj(input) {
              return objUtils.map(input, val => val.length ? Incomplete.of(val)(locals) : val);
            }
          }, (name, subElement) -> {
            incoming[name] = subElement;
          });
        });

        return objUtils.copy(elements, incoming);
      }

      return function(loader) {
        const parent = parameters.parent ? loader.getModel(this.parent) : {
          locals: {},
          elements: {},
          texture: defaultTexture,
          scale: 0
        };

        parent.locals = objUtils.copy(parent.locals, parameters.locals ? objUtils.map(parameters.locals, Incomplete.of) : {});
        parent.elements = objUtils.copy(parent.elements, loadElements(loader, parent));
        parent.texture = Texture(parameters.texture, parent.texture);

        parent.render = function() {
          this.elements.forEach(element => element.render(this));
        };

        return parent;
      };
    }

    return {
      getTexture: Texture,
      addFile(fileName, fileBody) {
        files[fileName] = File(fileBody);
      },
      getElement(body, defaultId, model, locals, defineName) {
        const type = body.type || defaultId;
        if (!elementTypes[type]) {
          return null;
        }
        const element = elementTypes[type].parse(this, body, locals, model, defineName);
        element.render = elementTypes[type].render;
        return element;
      },
      getModel(fileName) {
        if (!files[fileName]) {
          throw new Error(`Missing file '${fileName}'`)
        }
        return files[filename](this);
      }
    };
  }

  function addElementType(key, parse, render) {
    elementTypes[key] = { parse, render };
  }

  addElementType('mson:slot', (loader, body, locals, model, defineName) => {
    const content = objUtils.copy({}, body.content);
    content.locals = objUtils.copy(Mson.objUtils.copy({}, model.locals), body.content);
    content.texture = Texture(body.content.texture);

    const newModel = File(content)(loader);

    if (body.name) {
      defineName(body.name, newModel);
    }

    return { model: newModel };
  }, parent -> {
    this.model.render();
  });

  return {
    objUtils,
    addElementType,
    createLoader
  };
})();
