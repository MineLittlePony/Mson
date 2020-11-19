/*export*/ const objUtils = {
  /**
   * Converts the values of an object into another form.
   *
   * @param {object} obj An object with values to remap
   * @param {Function} valueMapper A mapping function to convert each value found in the supplied dictionary.
   */
  map(obj, valueMapper, keyMapper) {
    keyMapper = keyMapper || (a => a);
    const result = {};
    Object.keys(obj).forEach(key => result[keyMapper(key)] = valueMapper(obj[key]));
    return result;
  },
  /**
   * Copies all of the members from one object to another.
   *
   * @param {object} to The object to copy into
   * @param {object} from The object to copy from
   * @return {object} to
   */
  copy(to, from) {
    Object.keys(from).forEach(key => to[key] = from[key]);
    return to;
  },
  /**
   * Creates a shallow duplicate of the given object.
   *
   * @param {object} from The object to clone
   * @return {object} The newly cloned object with the same key-value mappings as the input.
   */
  clone(from) {
    return objUtils.copy({}, from);
  },
  /**
   * Extracts a subset of an object.
   *
   * @param {object} obj The object with one or more key-value mappings.
   * @param {Array} An array of keys for properties to extract.
   * @return {object} A new object only containing the permitted mappings.
   */
  subset(obj, keys) {
    const result = {};
    keys.forEach(key => result[key] = obj[key]);
    return result;
  },
  /**
   * Returns the first value that is not null or undefined.
   *
   * @param {any} vals The values to pick from
   * @return The first value that is not null or undefined.
   */
  first(...vals) {
    return vals.find(v => v !== null && v !== undefined);
  }
};
