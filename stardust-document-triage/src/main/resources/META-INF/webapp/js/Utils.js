define([], function() {
  return {
    getUuid: getUuid,
    inherit: inherit,
    inheritMethods: inheritMethods,
    formatDateTime: formatDateTime,
    getTimeDifferenceInMinutes: getTimeDifferenceInMinutes,
    applyDelayed: applyDelayed,
    removeItemFromArray: function(array, item) {
      removeItemFromArray(array, item);
    },
    formatMoneyAmount: formatMoneyAmount,
    getHash: getHash,
    getQueryParameters: getQueryParameters,
    installPostMessageListener: installPostMessageListener,
    updatePageURL: updatePageURL,
    composeUrl: composeUrl
  };

  /**
   * 
   */
  function getUuid() {
    if (!document.uuid) {
      document.uuid = 100000;
    }

    document.uuid++;

    return document.uuid;
  }

  /**
   * 
   */
  function inherit(target, source) {
    jQuery.extend(target, source);
    inheritMethods(target, source);

    return target;
  }

  /**
   * Auxiliary method to copy all methods from the parentObject to the childObject.
   */
  function inheritMethods(target, source) {
    for ( var member in source) {
      if (source[member] instanceof Function) {
        target[member] = source[member];
      }
    }
  }

  /**
   * 
   */
  function formatDateTime(dateTime) {
    return pad(dateTime.getUTCDate(), 2) + "." + pad(dateTime.getUTCMonth() + 1, 2) + "." + dateTime.getUTCFullYear() +
        " " + pad(dateTime.getUTCHours(), 2) + ":" + pad(dateTime.getUTCMinutes(), 2);
  }

  /**
   * 
   */
  function pad(number, characters) {
    return (1e15 + number + // combine with large number
    "" // convert to string
    ).slice(-characters); // cut leading "1"
  }

  /**
   * 
   * @param startTime
   * @param endTime
   * @returns
   */
  function getTimeDifferenceInMinutes(startTime, endTime) {
    var remainingTime = endTime - startTime;

    var minutes = remainingTime / 1000 / 60;
    var fraction = minutes % 1;

    return Math.round(minutes - fraction);
  }

  /**
   * Applies the result of a promise to the done function, invokes Angular apply safely on
   * success; if the promise failed, the error object is populated.
   */
  function applyDelayed(promise, scope, done, errors) {
    document.body.style.cursor = "wait";

    promise.done(function(data) {
      done(data);
      scope.safeApply();
      document.body.style.cursor = "default";
    }).fail(function(message) {
      if (message == null) {
        errors.push({
          message: "Server Error - Please contact iod.support@sungard.com"
        });
      } else {
        errors.push({
          message: message
        });
      }

      scope.safeApply();
      document.body.style.cursor = "default";
    });
  }

  /**
   * 
   * @param item
   */
  function removeItemFromArray(array, item) {
    var n = 0;
    while (n < array.length) {
      if (array[n] == item) {
        removeFromArray(array, n, n);
        // incase duplicates are present array size decreases,
        // so again checking with same index position
        continue;
      }
      ++n;
    }
  }

  function removeFromArray(array, from, to) {
    var rest = array.slice((to || from) + 1 || array.length);
    array.length = from < 0 ? array.length + from : from;
    return array.push.apply(array, rest);
  }

  /**
   * TODO Consider using existing library (e.g. google).
   */
  function formatMoneyAmount(amount, currency) {
    if (!amount) {
      amount = 0.0;
    }

    var decimalSeparator = new Number("1.2").toLocaleString().substr(1, 1);

    var amountWithCommas = amount.toLocaleString();
    var arParts = String(amountWithCommas).split(decimalSeparator);
    var intPart = arParts[0];
    var decPart = (arParts.length > 1 ? arParts[1] : '');
    decPart = (decPart + '00').substr(0, 2);

    return currency + " " + intPart + decimalSeparator + decPart;
  }

  /**
   * 
   */
  function getHash() {
    return window.location.hash.replace("#", "");
  }

  /**
   * 
   */
  function getQueryParameters() {
    var parameters = [];

    if (window.location.search != null) {
      var keyValues = window.location.search.slice(window.location.search.indexOf('?') + 1).split('&');

      for (var i = 0; i < keyValues.length; i++) {
        var keyValue = keyValues[i].split('=');

        parameters.push(keyValue[0]);

        parameters[keyValue[0]] = keyValue[1];
      }
    }

    return parameters;
  }

  function updatePageURL() {
    if (window.location.search.indexOf('?') > -1) {
      var currentUrl = window.location.href;
      var baseUrl = currentUrl.substring(0, currentUrl.indexOf('?'));
      var viewId = currentUrl.substring(currentUrl.indexOf("#"), currentUrl.length);
      var newUrl = baseUrl + viewId;
      // update the url without reloading page
      if (window.history.pushState) {
        window.history.pushState({}, window.document.title, newUrl);
      }
    }
  }

  /**
   * 
   */
  function installPostMessageListener(win, handler) {
    var ret = {};
    try {
      if (win.postMessage) {
        if (win.addEventListener) {
          win.addEventListener("message", handler, true);
        } else if (win.attachEvent) {
          win.attachEvent("onmessage", handler);
        } else {
          ret.errorCode = "NOT_SUPPORTED";
        }
      } else {
        ret.errorCode = "NOT_SUPPORTED";
      }
    } catch (e) {
      ret.errorCode = "FAILED";
      ret.errorMsg = e.message;
    }

    return ret;
  }

  /**
   * 
   * @param hash
   * @param parameters
   * @returns
   */
  function composeUrl(path, parameters, hash) {
    var url = path;
    var first = true;

    for ( var key in parameters) {
      if (first) {
        first = false;

        url += "?";
      } else {
        url += "&";
      }

      url += key;
      url += "=";
      url += parameters[key];
    }

    if (hash != null) {
      url += "#";
      url += hash;
    }

    return url;
  }
});