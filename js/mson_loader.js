// import { Incomplete } from './incomplete';
// import { objUtils } from './obj_utils';
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
 * skeletonModel.render({
 *  // TODO: rendering stuff.
 * });
 */
/*export*/ const Mson = (_ => {
  const reservedKeys = 'parent;locals;texture;scale'.split(';');
  const defaultTexture = createTexture([0, 0, 64, 32]);

  const elementTypes = {};

  function createTexture(body, parent) {
    body = body || {};
    parent = parent || {};
    if (body.length) {
      return {
        u: objUtils.first(body[0], parent.u, 0),
        v: objUtils.first(body[1], parent.v, 0),
        w: objUtils.first(body[2], parent.w, 0),
        h: objUtils.first(body[3], parent.h, 0)
      };
    }
    return objUtils.copy(objUtils.clone(parent), body);
  }

  /**
   * Creates a new model loader.
   */
  function createLoader() {
    const files = {};

    function createFile(body) {
      const parameters = body.substring ? JSON.parse(body) : body;

      function loadElements(loader, model) {
        const locals = model.locals;
        const incoming = {};
        const elementNames = Object.keys(parameters).filter(key => reservedKeys.indexOf(key) == -1);
        const elements = objUtils.map(objUtils.subset(parameters, elementNames), element => {
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
          }, (name, subElement) => {
            incoming[name] = subElement;
          });
        });

        return objUtils.copy(elements, incoming);
      }

      return {
        getSkeleton(loader) {
          const skeleton = parameters.parent ? getFile(parameters.parent).getSkeleton(loader) : {
            locals: {},
            elements: {},
            texture: defaultTexture,
            scale: 0
          };

          skeleton.locals = objUtils.copy(skeleton.locals, parameters.locals ? objUtils.map(parameters.locals, Incomplete.of) : {});
          skeleton.texture = createTexture(parameters.texture, skeleton.texture);

          return skeleton;
        },
        getElements(loader, skeleton) {
          if (parameters.parent) {
            getFile(parameters.parent).getElements(loader, skeleton);
          }
          skeleton.elements = objUtils.copy(skeleton.elements || {}, loadElements(loader, skeleton));
          return skeleton;
        },
        getModel(loader) {
          const parent = this.getElements(loader, this.getSkeleton(loader));
          parent.render = function(context) {
            Object.values(this.elements).forEach(element => element.render(this, context));
          };

          return parent;
        }
      };
    }

    function getFile(fileName) {
      if (!files[fileName]) {
        throw new Error(`Missing file '${fileName}'`)
      }
      return files[filename]
    }

    return {
      getTexture: createTexture,
      addFile(fileName, fileBody) {
        files[fileName] = createFile(fileBody);
      },
      getElement(body, defaultId, model, locals, defineName) {
        if (body.substring) {
          return createLink(body, model, locals);
        }

        const type = body.type || defaultId;
        if (!elementTypes[type]) {
          return null;
        }
        const element = elementTypes[type].parse(this, body, locals, model, defineName);
        element.render = elementTypes[type].render;
        return element;
      },
      getModel(fileName) {
        return getFile(fileName).getModel(this);
      }
    };
  }

  function createLink(id, model, locals) {
    if (id.indexOf('#') != 0) {
      throw new Error('link name should begin with a `#`.');
    }
    id = id.substring(1);
    let rendering;
    return {
      render(parent, context) {
        if (rendering) {
          throw new Error('Cyclic reference in link');
        }
        rendering = true;
        model.elements[id].render(parent, context);
        rendering = false;
      }
    }
  }

  /**
   * Adds functions to process the loading and redering of a particular element type.
   *
   * @param {String} key The unique identifier for the element type
   * @param {Function} parse Function called to parse an element's body.
   * @param {Function} render Function to call on the element when it's time to render it.
   */
  function addElementType(key, parse, render) {
    elementTypes[key] = { parse, render };
  }

  addElementType('mson:slot', (loader, body, locals, model, defineName) => {
    // slots have their own inheritance tree distinct from the host file
    // and variables defined on the slot itself are appended over what is inherited
    // from its included content, creating what is effectively a virtual model file
    // inserted into the host at the slot's position in the tree
    // i.e
    //              root_1
    //               |
    //               \/         root_2
    //              parent      |
    //               |         parent_2
    //               \/         |
    //              main_file  \/
    //               |        imported_file
    //               |       /
    //               |-slot\/
    //              self
    let content;
    if (typeof body.content === 'string') {
      content = { parent: body.content };
    } else {
      content = objUtils.clone(body.content);
    }
    content.locals = objUtils.copy(objUtils.clone(body.content.locals || {}), body.locals || {});
    content.texture = createTexture(body.texture, createTexture(body.content.texture));

    const newModel = { model: createFile(content).getModel(loader) };

    if (body.name) {
      defineName(body.name, newModel);
    }

    return newModel;
  }, (parent, context) => {
    this.model.render(context);
  });

  return {
    addElementType,
    createLoader
  };
})();
