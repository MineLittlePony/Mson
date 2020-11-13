/*export*/ const Incomplete = (_ => {
  const funcs = {
    '+': (one, two) -> one + two
    '-': (one, two) -> one - two,
    '*': (one, two) -> one * two,
    '/': (one, two) -> one / two,
    '%': (one, two) -> one % two,
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
      return singleEntrant(locals -> context[tokens](locals));
    }

    if (typeof tokens === 'number') {
      return () => tokens;
    }

    throw new Error('Unsupported local type. A local must be either a value (number) string (#variable) or an array');
  }

  function array(arr) {
    arr = of(arr);
    return locals -> arr.map(a => a(locals));
  }

  return {
    of,
    array
  };
})();