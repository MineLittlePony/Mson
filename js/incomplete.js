/*export*/ const Incomplete = (_ => {
  const funcs = {
    '+': (one, two) => one + two,
    '-': (one, two) => one - two,
    '*': (one, two) => one * two,
    '/': (one, two) => one / two,
    '%': (one, two) => one % two,
    '^': Math.pow
  };

  function singleEntrant(func) {
    let circularCheck;
    return function() {
      if (circularCheck) {
        throw new Error('Cyclical reference.');
      }
      circularCheck = true;
      const result = func.apply(this, arguments);
      circularCheck = false;
      return result;
    }
  }

  /**
   * Converts an unresolved variable name or equation into a function that will return the resolved value when called.
   *
   * @param {any} tokens Tokens to parse into an incomplete
   * @return {Function} A function that when called will return the completed value.
   */
  function of(tokens) {
    if (tokens.map) {
      if (tokens.length != 3) {
        throw new Error(`Saw a local of ${tokens.length} members. Expected 3 of (left, op, right).`);
      }
      const op = funcs[tokens[1]];
      if (!op) {
        throw new Error("Invalid operation. One of [+,-,*,/]");
      }

      left = of(tokens[0]);
      right = of(tokens[1]);
      return locals => op(left(locals), right(locals));
    }
    if (tokens.substring) {
      tokens = tokens.substring(1);
      return singleEntrant(locals => locals[tokens](locals));
    }

    if (typeof tokens === 'number') {
      return () => tokens;
    }

    throw new Error('Unsupported local type. A local must be either a value (number) string (#variable) or an array');
  }
  /**
   * Converts an array of unresolved variable names, or equations into a function that will return the resolved values array when called.
   * @param {Array} arr Array of tokens
   * @return {Function} A function that when called will return the completed array.
   */
  function array(arr) {
    arr = arr.map(of);
    return locals => arr.map(a => a(locals));
  }

  return { of, array };
})();