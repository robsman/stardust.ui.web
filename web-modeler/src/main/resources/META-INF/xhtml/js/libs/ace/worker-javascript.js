"no use strict";
;(function(window) {
if (typeof window.window != "undefined" && window.document) {
    return;
}

window.console = function() {
    var msgs = Array.prototype.slice.call(arguments, 0);
    postMessage({type: "log", data: msgs});
};
window.console.error =
window.console.warn = 
window.console.log =
window.console.trace = window.console;

window.window = window;
window.ace = window;

window.normalizeModule = function(parentId, moduleName) {
    if (moduleName.indexOf("!") !== -1) {
        var chunks = moduleName.split("!");
        return normalizeModule(parentId, chunks[0]) + "!" + normalizeModule(parentId, chunks[1]);
    }
    if (moduleName.charAt(0) == ".") {
        var base = parentId.split("/").slice(0, -1).join("/");
        moduleName = base + "/" + moduleName;
        
        while(moduleName.indexOf(".") !== -1 && previous != moduleName) {
            var previous = moduleName;
            moduleName = moduleName.replace(/\/\.\//, "/").replace(/[^\/]+\/\.\.\//, "");
        }
    }
    
    return moduleName;
};

window.require = function(parentId, id) {
    if (!id) {
        id = parentId
        parentId = null;
    }
    if (!id.charAt)
        throw new Error("worker.js require() accepts only (parentId, id) as arguments");

    id = normalizeModule(parentId, id);

    var module = require.modules[id];
    if (module) {
        if (!module.initialized) {
            module.initialized = true;
            module.exports = module.factory().exports;
        }
        return module.exports;
    }
    
    var chunks = id.split("/");
    chunks[0] = require.tlns[chunks[0]] || chunks[0];
    var path = chunks.join("/") + ".js";
    
    require.id = id;
    importScripts(path);
    return require(parentId, id);
};

require.modules = {};
require.tlns = {};

window.define = function(id, deps, factory) {
    if (arguments.length == 2) {
        factory = deps;
        if (typeof id != "string") {
            deps = id;
            id = require.id;
        }
    } else if (arguments.length == 1) {
        factory = id;
        id = require.id;
    }

    if (id.indexOf("text!") === 0) 
        return;
    
    var req = function(deps, factory) {
        return require(id, deps, factory);
    };

    require.modules[id] = {
        exports: {},
        factory: function() {
            var module = this;
            var returnExports = factory(req, module.exports, module);
            if (returnExports)
                module.exports = returnExports;
            return module;
        }
    };
};

window.initBaseUrls  = function initBaseUrls(topLevelNamespaces) {
    require.tlns = topLevelNamespaces;
}

window.initSender = function initSender() {

    var EventEmitter = require("ace/lib/event_emitter").EventEmitter;
    var oop = require("ace/lib/oop");
    
    var Sender = function() {};
    
    (function() {
        
        oop.implement(this, EventEmitter);
                
        this.callback = function(data, callbackId) {
            postMessage({
                type: "call",
                id: callbackId,
                data: data
            });
        };
    
        this.emit = function(name, data) {
            postMessage({
                type: "event",
                name: name,
                data: data
            });
        };
        
    }).call(Sender.prototype);
    
    return new Sender();
}

window.main = null;
window.sender = null;

window.onmessage = function(e) {
    var msg = e.data;
    if (msg.command) {
        if (main[msg.command])
            main[msg.command].apply(main, msg.args);
        else
            throw new Error("Unknown command:" + msg.command);
    }
    else if (msg.init) {        
        initBaseUrls(msg.tlns);
        require("ace/lib/es5-shim");
        sender = initSender();
        var clazz = require(msg.module)[msg.classname];
        main = new clazz(sender);
    } 
    else if (msg.event && sender) {
        sender._emit(msg.event, msg.data);
    }
};
})(this);

ace.define('ace/lib/event_emitter', ['require', 'exports', 'module' ], function(require, exports, module) {


var EventEmitter = {};
var stopPropagation = function() { this.propagationStopped = true; };
var preventDefault = function() { this.defaultPrevented = true; };

EventEmitter._emit =
EventEmitter._dispatchEvent = function(eventName, e) {
    this._eventRegistry || (this._eventRegistry = {});
    this._defaultHandlers || (this._defaultHandlers = {});

    var listeners = this._eventRegistry[eventName] || [];
    var defaultHandler = this._defaultHandlers[eventName];
    if (!listeners.length && !defaultHandler)
        return;

    if (typeof e != "object" || !e)
        e = {};

    if (!e.type)
        e.type = eventName;
    if (!e.stopPropagation)
        e.stopPropagation = stopPropagation;
    if (!e.preventDefault)
        e.preventDefault = preventDefault;

    for (var i=0; i<listeners.length; i++) {
        listeners[i](e, this);
        if (e.propagationStopped)
            break;
    }
    
    if (defaultHandler && !e.defaultPrevented)
        return defaultHandler(e, this);
};


EventEmitter._signal = function(eventName, e) {
    var listeners = (this._eventRegistry || {})[eventName];
    if (!listeners)
        return;

    for (var i=0; i<listeners.length; i++)
        listeners[i](e, this);
};

EventEmitter.once = function(eventName, callback) {
    var _self = this;
    callback && this.addEventListener(eventName, function newCallback() {
        _self.removeEventListener(eventName, newCallback);
        callback.apply(null, arguments);
    });
};


EventEmitter.setDefaultHandler = function(eventName, callback) {
    var handlers = this._defaultHandlers
    if (!handlers)
        handlers = this._defaultHandlers = {_disabled_: {}};
    
    if (handlers[eventName]) {
        var old = handlers[eventName];
        var disabled = handlers._disabled_[eventName];
        if (!disabled)
            handlers._disabled_[eventName] = disabled = [];
        disabled.push(old);
        var i = disabled.indexOf(callback);
        if (i != -1) 
            disabled.splice(i, 1);
    }
    handlers[eventName] = callback;
};
EventEmitter.removeDefaultHandler = function(eventName, callback) {
    var handlers = this._defaultHandlers
    if (!handlers)
        return;
    var disabled = handlers._disabled_[eventName];
    
    if (handlers[eventName] == callback) {
        var old = handlers[eventName];
        if (disabled)
            this.setDefaultHandler(eventName, disabled.pop());
    } else if (disabled) {
        var i = disabled.indexOf(callback);
        if (i != -1)
            disabled.splice(i, 1);
    }
};

EventEmitter.on =
EventEmitter.addEventListener = function(eventName, callback, capturing) {
    this._eventRegistry = this._eventRegistry || {};

    var listeners = this._eventRegistry[eventName];
    if (!listeners)
        listeners = this._eventRegistry[eventName] = [];

    if (listeners.indexOf(callback) == -1)
        listeners[capturing ? "unshift" : "push"](callback);
    return callback;
};

EventEmitter.off =
EventEmitter.removeListener =
EventEmitter.removeEventListener = function(eventName, callback) {
    this._eventRegistry = this._eventRegistry || {};

    var listeners = this._eventRegistry[eventName];
    if (!listeners)
        return;

    var index = listeners.indexOf(callback);
    if (index !== -1)
        listeners.splice(index, 1);
};

EventEmitter.removeAllListeners = function(eventName) {
    if (this._eventRegistry) this._eventRegistry[eventName] = [];
};

exports.EventEmitter = EventEmitter;

});

ace.define('ace/lib/oop', ['require', 'exports', 'module' ], function(require, exports, module) {


exports.inherits = (function() {
    var tempCtor = function() {};
    return function(ctor, superCtor) {
        tempCtor.prototype = superCtor.prototype;
        ctor.super_ = superCtor.prototype;
        ctor.prototype = new tempCtor();
        ctor.prototype.constructor = ctor;
    };
}());

exports.mixin = function(obj, mixin) {
    for (var key in mixin) {
        obj[key] = mixin[key];
    }
    return obj;
};

exports.implement = function(proto, mixin) {
    exports.mixin(proto, mixin);
};

});

ace.define('ace/lib/es5-shim', ['require', 'exports', 'module' ], function(require, exports, module) {

function Empty() {}

if (!Function.prototype.bind) {
    Function.prototype.bind = function bind(that) { // .length is 1
        var target = this;
        if (typeof target != "function") {
            throw new TypeError("Function.prototype.bind called on incompatible " + target);
        }
        var args = slice.call(arguments, 1); // for normal call
        var bound = function () {

            if (this instanceof bound) {

                var result = target.apply(
                    this,
                    args.concat(slice.call(arguments))
                );
                if (Object(result) === result) {
                    return result;
                }
                return this;

            } else {
                return target.apply(
                    that,
                    args.concat(slice.call(arguments))
                );

            }

        };
        if(target.prototype) {
            Empty.prototype = target.prototype;
            bound.prototype = new Empty();
            Empty.prototype = null;
        }
        return bound;
    };
}
var call = Function.prototype.call;
var prototypeOfArray = Array.prototype;
var prototypeOfObject = Object.prototype;
var slice = prototypeOfArray.slice;
var _toString = call.bind(prototypeOfObject.toString);
var owns = call.bind(prototypeOfObject.hasOwnProperty);
var defineGetter;
var defineSetter;
var lookupGetter;
var lookupSetter;
var supportsAccessors;
if ((supportsAccessors = owns(prototypeOfObject, "__defineGetter__"))) {
    defineGetter = call.bind(prototypeOfObject.__defineGetter__);
    defineSetter = call.bind(prototypeOfObject.__defineSetter__);
    lookupGetter = call.bind(prototypeOfObject.__lookupGetter__);
    lookupSetter = call.bind(prototypeOfObject.__lookupSetter__);
}
if ([1,2].splice(0).length != 2) {
    if(function() { // test IE < 9 to splice bug - see issue #138
        function makeArray(l) {
            var a = new Array(l+2);
            a[0] = a[1] = 0;
            return a;
        }
        var array = [], lengthBefore;
        
        array.splice.apply(array, makeArray(20));
        array.splice.apply(array, makeArray(26));

        lengthBefore = array.length; //46
        array.splice(5, 0, "XXX"); // add one element

        lengthBefore + 1 == array.length

        if (lengthBefore + 1 == array.length) {
            return true;// has right splice implementation without bugs
        }
    }()) {//IE 6/7
        var array_splice = Array.prototype.splice;
        Array.prototype.splice = function(start, deleteCount) {
            if (!arguments.length) {
                return [];
            } else {
                return array_splice.apply(this, [
                    start === void 0 ? 0 : start,
                    deleteCount === void 0 ? (this.length - start) : deleteCount
                ].concat(slice.call(arguments, 2)))
            }
        };
    } else {//IE8
        Array.prototype.splice = function(pos, removeCount){
            var length = this.length;
            if (pos > 0) {
                if (pos > length)
                    pos = length;
            } else if (pos == void 0) {
                pos = 0;
            } else if (pos < 0) {
                pos = Math.max(length + pos, 0);
            }

            if (!(pos+removeCount < length))
                removeCount = length - pos;

            var removed = this.slice(pos, pos+removeCount);
            var insert = slice.call(arguments, 2);
            var add = insert.length;            
            if (pos === length) {
                if (add) {
                    this.push.apply(this, insert);
                }
            } else {
                var remove = Math.min(removeCount, length - pos);
                var tailOldPos = pos + remove;
                var tailNewPos = tailOldPos + add - remove;
                var tailCount = length - tailOldPos;
                var lengthAfterRemove = length - remove;

                if (tailNewPos < tailOldPos) { // case A
                    for (var i = 0; i < tailCount; ++i) {
                        this[tailNewPos+i] = this[tailOldPos+i];
                    }
                } else if (tailNewPos > tailOldPos) { // case B
                    for (i = tailCount; i--; ) {
                        this[tailNewPos+i] = this[tailOldPos+i];
                    }
                } // else, add == remove (nothing to do)

                if (add && pos === lengthAfterRemove) {
                    this.length = lengthAfterRemove; // truncate array
                    this.push.apply(this, insert);
                } else {
                    this.length = lengthAfterRemove + add; // reserves space
                    for (i = 0; i < add; ++i) {
                        this[pos+i] = insert[i];
                    }
                }
            }
            return removed;
        };
    }
}
if (!Array.isArray) {
    Array.isArray = function isArray(obj) {
        return _toString(obj) == "[object Array]";
    };
}
var boxedString = Object("a"),
    splitString = boxedString[0] != "a" || !(0 in boxedString);

if (!Array.prototype.forEach) {
    Array.prototype.forEach = function forEach(fun /*, thisp*/) {
        var object = toObject(this),
            self = splitString && _toString(this) == "[object String]" ?
                this.split("") :
                object,
            thisp = arguments[1],
            i = -1,
            length = self.length >>> 0;
        if (_toString(fun) != "[object Function]") {
            throw new TypeError(); // TODO message
        }

        while (++i < length) {
            if (i in self) {
                fun.call(thisp, self[i], i, object);
            }
        }
    };
}
if (!Array.prototype.map) {
    Array.prototype.map = function map(fun /*, thisp*/) {
        var object = toObject(this),
            self = splitString && _toString(this) == "[object String]" ?
                this.split("") :
                object,
            length = self.length >>> 0,
            result = Array(length),
            thisp = arguments[1];
        if (_toString(fun) != "[object Function]") {
            throw new TypeError(fun + " is not a function");
        }

        for (var i = 0; i < length; i++) {
            if (i in self)
                result[i] = fun.call(thisp, self[i], i, object);
        }
        return result;
    };
}
if (!Array.prototype.filter) {
    Array.prototype.filter = function filter(fun /*, thisp */) {
        var object = toObject(this),
            self = splitString && _toString(this) == "[object String]" ?
                this.split("") :
                    object,
            length = self.length >>> 0,
            result = [],
            value,
            thisp = arguments[1];
        if (_toString(fun) != "[object Function]") {
            throw new TypeError(fun + " is not a function");
        }

        for (var i = 0; i < length; i++) {
            if (i in self) {
                value = self[i];
                if (fun.call(thisp, value, i, object)) {
                    result.push(value);
                }
            }
        }
        return result;
    };
}
if (!Array.prototype.every) {
    Array.prototype.every = function every(fun /*, thisp */) {
        var object = toObject(this),
            self = splitString && _toString(this) == "[object String]" ?
                this.split("") :
                object,
            length = self.length >>> 0,
            thisp = arguments[1];
        if (_toString(fun) != "[object Function]") {
            throw new TypeError(fun + " is not a function");
        }

        for (var i = 0; i < length; i++) {
            if (i in self && !fun.call(thisp, self[i], i, object)) {
                return false;
            }
        }
        return true;
    };
}
if (!Array.prototype.some) {
    Array.prototype.some = function some(fun /*, thisp */) {
        var object = toObject(this),
            self = splitString && _toString(this) == "[object String]" ?
                this.split("") :
                object,
            length = self.length >>> 0,
            thisp = arguments[1];
        if (_toString(fun) != "[object Function]") {
            throw new TypeError(fun + " is not a function");
        }

        for (var i = 0; i < length; i++) {
            if (i in self && fun.call(thisp, self[i], i, object)) {
                return true;
            }
        }
        return false;
    };
}
if (!Array.prototype.reduce) {
    Array.prototype.reduce = function reduce(fun /*, initial*/) {
        var object = toObject(this),
            self = splitString && _toString(this) == "[object String]" ?
                this.split("") :
                object,
            length = self.length >>> 0;
        if (_toString(fun) != "[object Function]") {
            throw new TypeError(fun + " is not a function");
        }
        if (!length && arguments.length == 1) {
            throw new TypeError("reduce of empty array with no initial value");
        }

        var i = 0;
        var result;
        if (arguments.length >= 2) {
            result = arguments[1];
        } else {
            do {
                if (i in self) {
                    result = self[i++];
                    break;
                }
                if (++i >= length) {
                    throw new TypeError("reduce of empty array with no initial value");
                }
            } while (true);
        }

        for (; i < length; i++) {
            if (i in self) {
                result = fun.call(void 0, result, self[i], i, object);
            }
        }

        return result;
    };
}
if (!Array.prototype.reduceRight) {
    Array.prototype.reduceRight = function reduceRight(fun /*, initial*/) {
        var object = toObject(this),
            self = splitString && _toString(this) == "[object String]" ?
                this.split("") :
                object,
            length = self.length >>> 0;
        if (_toString(fun) != "[object Function]") {
            throw new TypeError(fun + " is not a function");
        }
        if (!length && arguments.length == 1) {
            throw new TypeError("reduceRight of empty array with no initial value");
        }

        var result, i = length - 1;
        if (arguments.length >= 2) {
            result = arguments[1];
        } else {
            do {
                if (i in self) {
                    result = self[i--];
                    break;
                }
                if (--i < 0) {
                    throw new TypeError("reduceRight of empty array with no initial value");
                }
            } while (true);
        }

        do {
            if (i in this) {
                result = fun.call(void 0, result, self[i], i, object);
            }
        } while (i--);

        return result;
    };
}
if (!Array.prototype.indexOf || ([0, 1].indexOf(1, 2) != -1)) {
    Array.prototype.indexOf = function indexOf(sought /*, fromIndex */ ) {
        var self = splitString && _toString(this) == "[object String]" ?
                this.split("") :
                toObject(this),
            length = self.length >>> 0;

        if (!length) {
            return -1;
        }

        var i = 0;
        if (arguments.length > 1) {
            i = toInteger(arguments[1]);
        }
        i = i >= 0 ? i : Math.max(0, length + i);
        for (; i < length; i++) {
            if (i in self && self[i] === sought) {
                return i;
            }
        }
        return -1;
    };
}
if (!Array.prototype.lastIndexOf || ([0, 1].lastIndexOf(0, -3) != -1)) {
    Array.prototype.lastIndexOf = function lastIndexOf(sought /*, fromIndex */) {
        var self = splitString && _toString(this) == "[object String]" ?
                this.split("") :
                toObject(this),
            length = self.length >>> 0;

        if (!length) {
            return -1;
        }
        var i = length - 1;
        if (arguments.length > 1) {
            i = Math.min(i, toInteger(arguments[1]));
        }
        i = i >= 0 ? i : length - Math.abs(i);
        for (; i >= 0; i--) {
            if (i in self && sought === self[i]) {
                return i;
            }
        }
        return -1;
    };
}
if (!Object.getPrototypeOf) {
    Object.getPrototypeOf = function getPrototypeOf(object) {
        return object.__proto__ || (
            object.constructor ?
            object.constructor.prototype :
            prototypeOfObject
        );
    };
}
if (!Object.getOwnPropertyDescriptor) {
    var ERR_NON_OBJECT = "Object.getOwnPropertyDescriptor called on a " +
                         "non-object: ";
    Object.getOwnPropertyDescriptor = function getOwnPropertyDescriptor(object, property) {
        if ((typeof object != "object" && typeof object != "function") || object === null)
            throw new TypeError(ERR_NON_OBJECT + object);
        if (!owns(object, property))
            return;

        var descriptor, getter, setter;
        descriptor =  { enumerable: true, configurable: true };
        if (supportsAccessors) {
            var prototype = object.__proto__;
            object.__proto__ = prototypeOfObject;

            var getter = lookupGetter(object, property);
            var setter = lookupSetter(object, property);
            object.__proto__ = prototype;

            if (getter || setter) {
                if (getter) descriptor.get = getter;
                if (setter) descriptor.set = setter;
                return descriptor;
            }
        }
        descriptor.value = object[property];
        return descriptor;
    };
}
if (!Object.getOwnPropertyNames) {
    Object.getOwnPropertyNames = function getOwnPropertyNames(object) {
        return Object.keys(object);
    };
}
if (!Object.create) {
    var createEmpty;
    if (Object.prototype.__proto__ === null) {
        createEmpty = function () {
            return { "__proto__": null };
        };
    } else {
        createEmpty = function () {
            var empty = {};
            for (var i in empty)
                empty[i] = null;
            empty.constructor =
            empty.hasOwnProperty =
            empty.propertyIsEnumerable =
            empty.isPrototypeOf =
            empty.toLocaleString =
            empty.toString =
            empty.valueOf =
            empty.__proto__ = null;
            return empty;
        }
    }

    Object.create = function create(prototype, properties) {
        var object;
        if (prototype === null) {
            object = createEmpty();
        } else {
            if (typeof prototype != "object")
                throw new TypeError("typeof prototype["+(typeof prototype)+"] != 'object'");
            var Type = function () {};
            Type.prototype = prototype;
            object = new Type();
            object.__proto__ = prototype;
        }
        if (properties !== void 0)
            Object.defineProperties(object, properties);
        return object;
    };
}

function doesDefinePropertyWork(object) {
    try {
        Object.defineProperty(object, "sentinel", {});
        return "sentinel" in object;
    } catch (exception) {
    }
}
if (Object.defineProperty) {
    var definePropertyWorksOnObject = doesDefinePropertyWork({});
    var definePropertyWorksOnDom = typeof document == "undefined" ||
        doesDefinePropertyWork(document.createElement("div"));
    if (!definePropertyWorksOnObject || !definePropertyWorksOnDom) {
        var definePropertyFallback = Object.defineProperty;
    }
}

if (!Object.defineProperty || definePropertyFallback) {
    var ERR_NON_OBJECT_DESCRIPTOR = "Property description must be an object: ";
    var ERR_NON_OBJECT_TARGET = "Object.defineProperty called on non-object: "
    var ERR_ACCESSORS_NOT_SUPPORTED = "getters & setters can not be defined " +
                                      "on this javascript engine";

    Object.defineProperty = function defineProperty(object, property, descriptor) {
        if ((typeof object != "object" && typeof object != "function") || object === null)
            throw new TypeError(ERR_NON_OBJECT_TARGET + object);
        if ((typeof descriptor != "object" && typeof descriptor != "function") || descriptor === null)
            throw new TypeError(ERR_NON_OBJECT_DESCRIPTOR + descriptor);
        if (definePropertyFallback) {
            try {
                return definePropertyFallback.call(Object, object, property, descriptor);
            } catch (exception) {
            }
        }
        if (owns(descriptor, "value")) {

            if (supportsAccessors && (lookupGetter(object, property) ||
                                      lookupSetter(object, property)))
            {
                var prototype = object.__proto__;
                object.__proto__ = prototypeOfObject;
                delete object[property];
                object[property] = descriptor.value;
                object.__proto__ = prototype;
            } else {
                object[property] = descriptor.value;
            }
        } else {
            if (!supportsAccessors)
                throw new TypeError(ERR_ACCESSORS_NOT_SUPPORTED);
            if (owns(descriptor, "get"))
                defineGetter(object, property, descriptor.get);
            if (owns(descriptor, "set"))
                defineSetter(object, property, descriptor.set);
        }

        return object;
    };
}
if (!Object.defineProperties) {
    Object.defineProperties = function defineProperties(object, properties) {
        for (var property in properties) {
            if (owns(properties, property))
                Object.defineProperty(object, property, properties[property]);
        }
        return object;
    };
}
if (!Object.seal) {
    Object.seal = function seal(object) {
        return object;
    };
}
if (!Object.freeze) {
    Object.freeze = function freeze(object) {
        return object;
    };
}
try {
    Object.freeze(function () {});
} catch (exception) {
    Object.freeze = (function freeze(freezeObject) {
        return function freeze(object) {
            if (typeof object == "function") {
                return object;
            } else {
                return freezeObject(object);
            }
        };
    })(Object.freeze);
}
if (!Object.preventExtensions) {
    Object.preventExtensions = function preventExtensions(object) {
        return object;
    };
}
if (!Object.isSealed) {
    Object.isSealed = function isSealed(object) {
        return false;
    };
}
if (!Object.isFrozen) {
    Object.isFrozen = function isFrozen(object) {
        return false;
    };
}
if (!Object.isExtensible) {
    Object.isExtensible = function isExtensible(object) {
        if (Object(object) === object) {
            throw new TypeError(); // TODO message
        }
        var name = '';
        while (owns(object, name)) {
            name += '?';
        }
        object[name] = true;
        var returnValue = owns(object, name);
        delete object[name];
        return returnValue;
    };
}
if (!Object.keys) {
    var hasDontEnumBug = true,
        dontEnums = [
            "toString",
            "toLocaleString",
            "valueOf",
            "hasOwnProperty",
            "isPrototypeOf",
            "propertyIsEnumerable",
            "constructor"
        ],
        dontEnumsLength = dontEnums.length;

    for (var key in {"toString": null}) {
        hasDontEnumBug = false;
    }

    Object.keys = function keys(object) {

        if (
            (typeof object != "object" && typeof object != "function") ||
            object === null
        ) {
            throw new TypeError("Object.keys called on a non-object");
        }

        var keys = [];
        for (var name in object) {
            if (owns(object, name)) {
                keys.push(name);
            }
        }

        if (hasDontEnumBug) {
            for (var i = 0, ii = dontEnumsLength; i < ii; i++) {
                var dontEnum = dontEnums[i];
                if (owns(object, dontEnum)) {
                    keys.push(dontEnum);
                }
            }
        }
        return keys;
    };

}
if (!Date.now) {
    Date.now = function now() {
        return new Date().getTime();
    };
}
var ws = "\x09\x0A\x0B\x0C\x0D\x20\xA0\u1680\u180E\u2000\u2001\u2002\u2003" +
    "\u2004\u2005\u2006\u2007\u2008\u2009\u200A\u202F\u205F\u3000\u2028" +
    "\u2029\uFEFF";
if (!String.prototype.trim || ws.trim()) {
    ws = "[" + ws + "]";
    var trimBeginRegexp = new RegExp("^" + ws + ws + "*"),
        trimEndRegexp = new RegExp(ws + ws + "*$");
    String.prototype.trim = function trim() {
        return String(this).replace(trimBeginRegexp, "").replace(trimEndRegexp, "");
    };
}

function toInteger(n) {
    n = +n;
    if (n !== n) { // isNaN
        n = 0;
    } else if (n !== 0 && n !== (1/0) && n !== -(1/0)) {
        n = (n > 0 || -1) * Math.floor(Math.abs(n));
    }
    return n;
}

function isPrimitive(input) {
    var type = typeof input;
    return (
        input === null ||
        type === "undefined" ||
        type === "boolean" ||
        type === "number" ||
        type === "string"
    );
}

function toPrimitive(input) {
    var val, valueOf, toString;
    if (isPrimitive(input)) {
        return input;
    }
    valueOf = input.valueOf;
    if (typeof valueOf === "function") {
        val = valueOf.call(input);
        if (isPrimitive(val)) {
            return val;
        }
    }
    toString = input.toString;
    if (typeof toString === "function") {
        val = toString.call(input);
        if (isPrimitive(val)) {
            return val;
        }
    }
    throw new TypeError();
}
var toObject = function (o) {
    if (o == null) { // this matches both null and undefined
        throw new TypeError("can't convert "+o+" to object");
    }
    return Object(o);
};

});

ace.define('ace/mode/javascript_worker', ['require', 'exports', 'module' , 'ace/lib/oop', 'ace/worker/mirror', 'ace/mode/javascript/jshint'], function(require, exports, module) {


var oop = require("../lib/oop");
var Mirror = require("../worker/mirror").Mirror;
var lint = require("./javascript/jshint").JSHINT;

function startRegex(arr) {
    return RegExp("^(" + arr.join("|") + ")");
}

var disabledWarningsRe = startRegex([
    "Bad for in variable '(.+)'.",
    'Missing "use strict"'
]);
var errorsRe = startRegex([
    "Unexpected",
    "Expected ",
    "Confusing (plus|minus)",
    "\\{a\\} unterminated regular expression",
    "Unclosed ",
    "Unmatched ",
    "Unbegun comment",
    "Bad invocation",
    "Missing space after",
    "Missing operator at"
]);
var infoRe = startRegex([
    "Expected an assignment",
    "Bad escapement of EOL",
    "Unexpected comma",
    "Unexpected space",
    "Missing radix parameter.",
    "A leading decimal point can",
    "\\['{a}'\\] is better written in dot notation.",
    "'{a}' used out of scope"
]);

var JavaScriptWorker = exports.JavaScriptWorker = function(sender) {
    Mirror.call(this, sender);
    this.setTimeout(500);
    this.setOptions();
};

oop.inherits(JavaScriptWorker, Mirror);

(function() {
    this.setOptions = function(options) {
        this.options = options || {
            esnext: true,
            moz: true,
            devel: true,
            browser: true,
            node: true,
            laxcomma: true,
            laxbreak: true,
            lastsemic: true,
            onevar: false,
            passfail: false,
            maxerr: 100,
            expr: true,
            multistr: true,
            globalstrict: true
        };
        this.doc.getValue() && this.deferredUpdate.schedule(100);
    };

    this.changeOptions = function(newOptions) {
        oop.mixin(this.options, newOptions);
        this.doc.getValue() && this.deferredUpdate.schedule(100);
    };

    this.isValidJS = function(str) {
        try {
            eval("throw 0;" + str);
        } catch(e) {
            if (e === 0)
                return true;
        }
        return false
    };

    this.onUpdate = function() {
        var value = this.doc.getValue();
        value = value.replace(/^#!.*\n/, "\n");
        if (!value) {
            this.sender.emit("jslint", []);
            return;
        }
        var errors = [];
        var maxErrorLevel = this.isValidJS(value) ? "warning" : "error";
        lint(value, this.options);
        var results = lint.errors;

        var errorAdded = false
        for (var i = 0; i < results.length; i++) {
            var error = results[i];
            if (!error)
                continue;
            var raw = error.raw;
            var type = "warning";

            if (raw == "Missing semicolon.") {
                var str = error.evidence.substr(error.character);
                str = str.charAt(str.search(/\S/));
                if (maxErrorLevel == "error" && str && /[\w\d{(['"]/.test(str)) {
                    error.reason = 'Missing ";" before statement';
                    type = "error";
                } else {
                    type = "info";
                }
            }
            else if (disabledWarningsRe.test(raw)) {
                continue;
            }
            else if (infoRe.test(raw)) {
                type = "info"
            }
            else if (errorsRe.test(raw)) {
                errorAdded  = true;
                type = maxErrorLevel;
            }
            else if (raw == "'{a}' is not defined.") {
                type = "warning";
            }
            else if (raw == "'{a}' is defined but never used.") {
                type = "info";
            }

            errors.push({
                row: error.line-1,
                column: error.character-1,
                text: error.reason,
                type: type,
                raw: raw
            });

            if (errorAdded) {
            }
        }

        this.sender.emit("jslint", errors);
    };

}).call(JavaScriptWorker.prototype);

});
ace.define('ace/worker/mirror', ['require', 'exports', 'module' , 'ace/document', 'ace/lib/lang'], function(require, exports, module) {


var Document = require("../document").Document;
var lang = require("../lib/lang");
    
var Mirror = exports.Mirror = function(sender) {
    this.sender = sender;
    var doc = this.doc = new Document("");
    
    var deferredUpdate = this.deferredUpdate = lang.delayedCall(this.onUpdate.bind(this));
    
    var _self = this;
    sender.on("change", function(e) {
        doc.applyDeltas(e.data);
        deferredUpdate.schedule(_self.$timeout);
    });
};

(function() {
    
    this.$timeout = 500;
    
    this.setTimeout = function(timeout) {
        this.$timeout = timeout;
    };
    
    this.setValue = function(value) {
        this.doc.setValue(value);
        this.deferredUpdate.schedule(this.$timeout);
    };
    
    this.getValue = function(callbackId) {
        this.sender.callback(this.doc.getValue(), callbackId);
    };
    
    this.onUpdate = function() {
    };
    
}).call(Mirror.prototype);

});

ace.define('ace/document', ['require', 'exports', 'module' , 'ace/lib/oop', 'ace/lib/event_emitter', 'ace/range', 'ace/anchor'], function(require, exports, module) {


var oop = require("./lib/oop");
var EventEmitter = require("./lib/event_emitter").EventEmitter;
var Range = require("./range").Range;
var Anchor = require("./anchor").Anchor;

var Document = function(text) {
    this.$lines = [];
    if (text.length == 0) {
        this.$lines = [""];
    } else if (Array.isArray(text)) {
        this._insertLines(0, text);
    } else {
        this.insert({row: 0, column:0}, text);
    }
};

(function() {

    oop.implement(this, EventEmitter);
    this.setValue = function(text) {
        var len = this.getLength();
        this.remove(new Range(0, 0, len, this.getLine(len-1).length));
        this.insert({row: 0, column:0}, text);
    };
    this.getValue = function() {
        return this.getAllLines().join(this.getNewLineCharacter());
    };
    this.createAnchor = function(row, column) {
        return new Anchor(this, row, column);
    };
    if ("aaa".split(/a/).length == 0)
        this.$split = function(text) {
            return text.replace(/\r\n|\r/g, "\n").split("\n");
        }
    else
        this.$split = function(text) {
            return text.split(/\r\n|\r|\n/);
        };


    this.$detectNewLine = function(text) {
        var match = text.match(/^.*?(\r\n|\r|\n)/m);
        this.$autoNewLine = match ? match[1] : "\n";
    };
    this.getNewLineCharacter = function() {
        switch (this.$newLineMode) {
          case "windows":
            return "\r\n";
          case "unix":
            return "\n";
          default:
            return this.$autoNewLine;
        }
    };

    this.$autoNewLine = "\n";
    this.$newLineMode = "auto";
    this.setNewLineMode = function(newLineMode) {
        if (this.$newLineMode === newLineMode)
            return;

        this.$newLineMode = newLineMode;
    };
    this.getNewLineMode = function() {
        return this.$newLineMode;
    };
    this.isNewLine = function(text) {
        return (text == "\r\n" || text == "\r" || text == "\n");
    };
    this.getLine = function(row) {
        return this.$lines[row] || "";
    };
    this.getLines = function(firstRow, lastRow) {
        return this.$lines.slice(firstRow, lastRow + 1);
    };
    this.getAllLines = function() {
        return this.getLines(0, this.getLength());
    };
    this.getLength = function() {
        return this.$lines.length;
    };
    this.getTextRange = function(range) {
        if (range.start.row == range.end.row) {
            return this.getLine(range.start.row)
                .substring(range.start.column, range.end.column);
        }
        var lines = this.getLines(range.start.row, range.end.row);
        lines[0] = (lines[0] || "").substring(range.start.column);
        var l = lines.length - 1;
        if (range.end.row - range.start.row == l)
            lines[l] = lines[l].substring(0, range.end.column);
        return lines.join(this.getNewLineCharacter());
    };

    this.$clipPosition = function(position) {
        var length = this.getLength();
        if (position.row >= length) {
            position.row = Math.max(0, length - 1);
            position.column = this.getLine(length-1).length;
        } else if (position.row < 0)
            position.row = 0;
        return position;
    };
    this.insert = function(position, text) {
        if (!text || text.length === 0)
            return position;

        position = this.$clipPosition(position);
        if (this.getLength() <= 1)
            this.$detectNewLine(text);

        var lines = this.$split(text);
        var firstLine = lines.splice(0, 1)[0];
        var lastLine = lines.length == 0 ? null : lines.splice(lines.length - 1, 1)[0];

        position = this.insertInLine(position, firstLine);
        if (lastLine !== null) {
            position = this.insertNewLine(position); // terminate first line
            position = this._insertLines(position.row, lines);
            position = this.insertInLine(position, lastLine || "");
        }
        return position;
    };
    this.insertLines = function(row, lines) {
        if (row >= this.getLength())
            return this.insert({row: row, column: 0}, "\n" + lines.join("\n"));
        return this._insertLines(Math.max(row, 0), lines);
    };
    this._insertLines = function(row, lines) {
        if (lines.length == 0)
            return {row: row, column: 0};
        if (lines.length > 0xFFFF) {
            var end = this._insertLines(row, lines.slice(0xFFFF));
            lines = lines.slice(0, 0xFFFF);
        }

        var args = [row, 0];
        args.push.apply(args, lines);
        this.$lines.splice.apply(this.$lines, args);

        var range = new Range(row, 0, row + lines.length, 0);
        var delta = {
            action: "insertLines",
            range: range,
            lines: lines
        };
        this._emit("change", { data: delta });
        return end || range.end;
    };
    this.insertNewLine = function(position) {
        position = this.$clipPosition(position);
        var line = this.$lines[position.row] || "";

        this.$lines[position.row] = line.substring(0, position.column);
        this.$lines.splice(position.row + 1, 0, line.substring(position.column, line.length));

        var end = {
            row : position.row + 1,
            column : 0
        };

        var delta = {
            action: "insertText",
            range: Range.fromPoints(position, end),
            text: this.getNewLineCharacter()
        };
        this._emit("change", { data: delta });

        return end;
    };
    this.insertInLine = function(position, text) {
        if (text.length == 0)
            return position;

        var line = this.$lines[position.row] || "";

        this.$lines[position.row] = line.substring(0, position.column) + text
                + line.substring(position.column);

        var end = {
            row : position.row,
            column : position.column + text.length
        };

        var delta = {
            action: "insertText",
            range: Range.fromPoints(position, end),
            text: text
        };
        this._emit("change", { data: delta });

        return end;
    };
    this.remove = function(range) {
        range.start = this.$clipPosition(range.start);
        range.end = this.$clipPosition(range.end);

        if (range.isEmpty())
            return range.start;

        var firstRow = range.start.row;
        var lastRow = range.end.row;

        if (range.isMultiLine()) {
            var firstFullRow = range.start.column == 0 ? firstRow : firstRow + 1;
            var lastFullRow = lastRow - 1;

            if (range.end.column > 0)
                this.removeInLine(lastRow, 0, range.end.column);

            if (lastFullRow >= firstFullRow)
                this._removeLines(firstFullRow, lastFullRow);

            if (firstFullRow != firstRow) {
                this.removeInLine(firstRow, range.start.column, this.getLine(firstRow).length);
                this.removeNewLine(range.start.row);
            }
        }
        else {
            this.removeInLine(firstRow, range.start.column, range.end.column);
        }
        return range.start;
    };
    this.removeInLine = function(row, startColumn, endColumn) {
        if (startColumn == endColumn)
            return;

        var range = new Range(row, startColumn, row, endColumn);
        var line = this.getLine(row);
        var removed = line.substring(startColumn, endColumn);
        var newLine = line.substring(0, startColumn) + line.substring(endColumn, line.length);
        this.$lines.splice(row, 1, newLine);

        var delta = {
            action: "removeText",
            range: range,
            text: removed
        };
        this._emit("change", { data: delta });
        return range.start;
    };
    this.removeLines = function(firstRow, lastRow) {
        if (firstRow < 0 || lastRow >= this.getLength())
            return this.remove(new Range(firstRow, 0, lastRow + 1, 0));
        return this._removeLines(firstRow, lastRow);
    };

    this._removeLines = function(firstRow, lastRow) {
        var range = new Range(firstRow, 0, lastRow + 1, 0);
        var removed = this.$lines.splice(firstRow, lastRow - firstRow + 1);

        var delta = {
            action: "removeLines",
            range: range,
            nl: this.getNewLineCharacter(),
            lines: removed
        };
        this._emit("change", { data: delta });
        return removed;
    };
    this.removeNewLine = function(row) {
        var firstLine = this.getLine(row);
        var secondLine = this.getLine(row+1);

        var range = new Range(row, firstLine.length, row+1, 0);
        var line = firstLine + secondLine;

        this.$lines.splice(row, 2, line);

        var delta = {
            action: "removeText",
            range: range,
            text: this.getNewLineCharacter()
        };
        this._emit("change", { data: delta });
    };
    this.replace = function(range, text) {
        if (text.length == 0 && range.isEmpty())
            return range.start;
        if (text == this.getTextRange(range))
            return range.end;

        this.remove(range);
        if (text) {
            var end = this.insert(range.start, text);
        }
        else {
            end = range.start;
        }

        return end;
    };
    this.applyDeltas = function(deltas) {
        for (var i=0; i<deltas.length; i++) {
            var delta = deltas[i];
            var range = Range.fromPoints(delta.range.start, delta.range.end);

            if (delta.action == "insertLines")
                this.insertLines(range.start.row, delta.lines);
            else if (delta.action == "insertText")
                this.insert(range.start, delta.text);
            else if (delta.action == "removeLines")
                this._removeLines(range.start.row, range.end.row - 1);
            else if (delta.action == "removeText")
                this.remove(range);
        }
    };
    this.revertDeltas = function(deltas) {
        for (var i=deltas.length-1; i>=0; i--) {
            var delta = deltas[i];

            var range = Range.fromPoints(delta.range.start, delta.range.end);

            if (delta.action == "insertLines")
                this._removeLines(range.start.row, range.end.row - 1);
            else if (delta.action == "insertText")
                this.remove(range);
            else if (delta.action == "removeLines")
                this._insertLines(range.start.row, delta.lines);
            else if (delta.action == "removeText")
                this.insert(range.start, delta.text);
        }
    };
    this.indexToPosition = function(index, startRow) {
        var lines = this.$lines || this.getAllLines();
        var newlineLength = this.getNewLineCharacter().length;
        for (var i = startRow || 0, l = lines.length; i < l; i++) {
            index -= lines[i].length + newlineLength;
            if (index < 0)
                return {row: i, column: index + lines[i].length + newlineLength};
        }
        return {row: l-1, column: lines[l-1].length};
    };
    this.positionToIndex = function(pos, startRow) {
        var lines = this.$lines || this.getAllLines();
        var newlineLength = this.getNewLineCharacter().length;
        var index = 0;
        var row = Math.min(pos.row, lines.length);
        for (var i = startRow || 0; i < row; ++i)
            index += lines[i].length + newlineLength;

        return index + pos.column;
    };

}).call(Document.prototype);

exports.Document = Document;
});

ace.define('ace/range', ['require', 'exports', 'module' ], function(require, exports, module) {

var comparePoints = function(p1, p2) {
    return p1.row - p2.row || p1.column - p2.column;
};
var Range = function(startRow, startColumn, endRow, endColumn) {
    this.start = {
        row: startRow,
        column: startColumn
    };

    this.end = {
        row: endRow,
        column: endColumn
    };
};

(function() {
    this.isEqual = function(range) {
        return this.start.row === range.start.row &&
            this.end.row === range.end.row &&
            this.start.column === range.start.column &&
            this.end.column === range.end.column;
    };
    this.toString = function() {
        return ("Range: [" + this.start.row + "/" + this.start.column +
            "] -> [" + this.end.row + "/" + this.end.column + "]");
    };

    this.contains = function(row, column) {
        return this.compare(row, column) == 0;
    };
    this.compareRange = function(range) {
        var cmp,
            end = range.end,
            start = range.start;

        cmp = this.compare(end.row, end.column);
        if (cmp == 1) {
            cmp = this.compare(start.row, start.column);
            if (cmp == 1) {
                return 2;
            } else if (cmp == 0) {
                return 1;
            } else {
                return 0;
            }
        } else if (cmp == -1) {
            return -2;
        } else {
            cmp = this.compare(start.row, start.column);
            if (cmp == -1) {
                return -1;
            } else if (cmp == 1) {
                return 42;
            } else {
                return 0;
            }
        }
    };
    this.comparePoint = function(p) {
        return this.compare(p.row, p.column);
    };
    this.containsRange = function(range) {
        return this.comparePoint(range.start) == 0 && this.comparePoint(range.end) == 0;
    };
    this.intersects = function(range) {
        var cmp = this.compareRange(range);
        return (cmp == -1 || cmp == 0 || cmp == 1);
    };
    this.isEnd = function(row, column) {
        return this.end.row == row && this.end.column == column;
    };
    this.isStart = function(row, column) {
        return this.start.row == row && this.start.column == column;
    };
    this.setStart = function(row, column) {
        if (typeof row == "object") {
            this.start.column = row.column;
            this.start.row = row.row;
        } else {
            this.start.row = row;
            this.start.column = column;
        }
    };
    this.setEnd = function(row, column) {
        if (typeof row == "object") {
            this.end.column = row.column;
            this.end.row = row.row;
        } else {
            this.end.row = row;
            this.end.column = column;
        }
    };
    this.inside = function(row, column) {
        if (this.compare(row, column) == 0) {
            if (this.isEnd(row, column) || this.isStart(row, column)) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    };
    this.insideStart = function(row, column) {
        if (this.compare(row, column) == 0) {
            if (this.isEnd(row, column)) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    };
    this.insideEnd = function(row, column) {
        if (this.compare(row, column) == 0) {
            if (this.isStart(row, column)) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    };
    this.compare = function(row, column) {
        if (!this.isMultiLine()) {
            if (row === this.start.row) {
                return column < this.start.column ? -1 : (column > this.end.column ? 1 : 0);
            };
        }

        if (row < this.start.row)
            return -1;

        if (row > this.end.row)
            return 1;

        if (this.start.row === row)
            return column >= this.start.column ? 0 : -1;

        if (this.end.row === row)
            return column <= this.end.column ? 0 : 1;

        return 0;
    };
    this.compareStart = function(row, column) {
        if (this.start.row == row && this.start.column == column) {
            return -1;
        } else {
            return this.compare(row, column);
        }
    };
    this.compareEnd = function(row, column) {
        if (this.end.row == row && this.end.column == column) {
            return 1;
        } else {
            return this.compare(row, column);
        }
    };
    this.compareInside = function(row, column) {
        if (this.end.row == row && this.end.column == column) {
            return 1;
        } else if (this.start.row == row && this.start.column == column) {
            return -1;
        } else {
            return this.compare(row, column);
        }
    };
    this.clipRows = function(firstRow, lastRow) {
        if (this.end.row > lastRow)
            var end = {row: lastRow + 1, column: 0};
        else if (this.end.row < firstRow)
            var end = {row: firstRow, column: 0};

        if (this.start.row > lastRow)
            var start = {row: lastRow + 1, column: 0};
        else if (this.start.row < firstRow)
            var start = {row: firstRow, column: 0};

        return Range.fromPoints(start || this.start, end || this.end);
    };
    this.extend = function(row, column) {
        var cmp = this.compare(row, column);

        if (cmp == 0)
            return this;
        else if (cmp == -1)
            var start = {row: row, column: column};
        else
            var end = {row: row, column: column};

        return Range.fromPoints(start || this.start, end || this.end);
    };

    this.isEmpty = function() {
        return (this.start.row === this.end.row && this.start.column === this.end.column);
    };
    this.isMultiLine = function() {
        return (this.start.row !== this.end.row);
    };
    this.clone = function() {
        return Range.fromPoints(this.start, this.end);
    };
    this.collapseRows = function() {
        if (this.end.column == 0)
            return new Range(this.start.row, 0, Math.max(this.start.row, this.end.row-1), 0)
        else
            return new Range(this.start.row, 0, this.end.row, 0)
    };
    this.toScreenRange = function(session) {
        var screenPosStart = session.documentToScreenPosition(this.start);
        var screenPosEnd = session.documentToScreenPosition(this.end);

        return new Range(
            screenPosStart.row, screenPosStart.column,
            screenPosEnd.row, screenPosEnd.column
        );
    };
    this.moveBy = function(row, column) {
        this.start.row += row;
        this.start.column += column;
        this.end.row += row;
        this.end.column += column;
    };

}).call(Range.prototype);
Range.fromPoints = function(start, end) {
    return new Range(start.row, start.column, end.row, end.column);
};
Range.comparePoints = comparePoints;

Range.comparePoints = function(p1, p2) {
    return p1.row - p2.row || p1.column - p2.column;
};


exports.Range = Range;
});

ace.define('ace/anchor', ['require', 'exports', 'module' , 'ace/lib/oop', 'ace/lib/event_emitter'], function(require, exports, module) {


var oop = require("./lib/oop");
var EventEmitter = require("./lib/event_emitter").EventEmitter;

var Anchor = exports.Anchor = function(doc, row, column) {
    this.$onChange = this.onChange.bind(this);
    this.attach(doc);
    
    if (typeof column == "undefined")
        this.setPosition(row.row, row.column);
    else
        this.setPosition(row, column);
};

(function() {

    oop.implement(this, EventEmitter);
    this.getPosition = function() {
        return this.$clipPositionToDocument(this.row, this.column);
    };
    this.getDocument = function() {
        return this.document;
    };
    this.onChange = function(e) {
        var delta = e.data;
        var range = delta.range;

        if (range.start.row == range.end.row && range.start.row != this.row)
            return;

        if (range.start.row > this.row)
            return;

        if (range.start.row == this.row && range.start.column > this.column)
            return;

        var row = this.row;
        var column = this.column;
        var start = range.start;
        var end = range.end;

        if (delta.action === "insertText") {
            if (start.row === row && start.column <= column) {
                if (start.row === end.row) {
                    column += end.column - start.column;
                } else {
                    column -= start.column;
                    row += end.row - start.row;
                }
            } else if (start.row !== end.row && start.row < row) {
                row += end.row - start.row;
            }
        } else if (delta.action === "insertLines") {
            if (start.row <= row) {
                row += end.row - start.row;
            }
        } else if (delta.action === "removeText") {
            if (start.row === row && start.column < column) {
                if (end.column >= column)
                    column = start.column;
                else
                    column = Math.max(0, column - (end.column - start.column));

            } else if (start.row !== end.row && start.row < row) {
                if (end.row === row)
                    column = Math.max(0, column - end.column) + start.column;
                row -= (end.row - start.row);
            } else if (end.row === row) {
                row -= end.row - start.row;
                column = Math.max(0, column - end.column) + start.column;
            }
        } else if (delta.action == "removeLines") {
            if (start.row <= row) {
                if (end.row <= row)
                    row -= end.row - start.row;
                else {
                    row = start.row;
                    column = 0;
                }
            }
        }

        this.setPosition(row, column, true);
    };
    this.setPosition = function(row, column, noClip) {
        var pos;
        if (noClip) {
            pos = {
                row: row,
                column: column
            };
        } else {
            pos = this.$clipPositionToDocument(row, column);
        }

        if (this.row == pos.row && this.column == pos.column)
            return;

        var old = {
            row: this.row,
            column: this.column
        };

        this.row = pos.row;
        this.column = pos.column;
        this._emit("change", {
            old: old,
            value: pos
        });
    };
    this.detach = function() {
        this.document.removeEventListener("change", this.$onChange);
    };
    this.attach = function(doc) {
        this.document = doc || this.document;
        this.document.on("change", this.$onChange);
    };
    this.$clipPositionToDocument = function(row, column) {
        var pos = {};

        if (row >= this.document.getLength()) {
            pos.row = Math.max(0, this.document.getLength() - 1);
            pos.column = this.document.getLine(pos.row).length;
        }
        else if (row < 0) {
            pos.row = 0;
            pos.column = 0;
        }
        else {
            pos.row = row;
            pos.column = Math.min(this.document.getLine(pos.row).length, Math.max(0, column));
        }

        if (column < 0)
            pos.column = 0;

        return pos;
    };

}).call(Anchor.prototype);

});

ace.define('ace/lib/lang', ['require', 'exports', 'module' ], function(require, exports, module) {


exports.stringReverse = function(string) {
    return string.split("").reverse().join("");
};

exports.stringRepeat = function (string, count) {
    var result = '';
    while (count > 0) {
        if (count & 1)
            result += string;

        if (count >>= 1)
            string += string;
    }
    return result;
};

var trimBeginRegexp = /^\s\s*/;
var trimEndRegexp = /\s\s*$/;

exports.stringTrimLeft = function (string) {
    return string.replace(trimBeginRegexp, '');
};

exports.stringTrimRight = function (string) {
    return string.replace(trimEndRegexp, '');
};

exports.copyObject = function(obj) {
    var copy = {};
    for (var key in obj) {
        copy[key] = obj[key];
    }
    return copy;
};

exports.copyArray = function(array){
    var copy = [];
    for (var i=0, l=array.length; i<l; i++) {
        if (array[i] && typeof array[i] == "object")
            copy[i] = this.copyObject( array[i] );
        else 
            copy[i] = array[i];
    }
    return copy;
};

exports.deepCopy = function (obj) {
    if (typeof obj != "object") {
        return obj;
    }
    
    var copy = obj.constructor();
    for (var key in obj) {
        if (typeof obj[key] == "object") {
            copy[key] = this.deepCopy(obj[key]);
        } else {
            copy[key] = obj[key];
        }
    }
    return copy;
};

exports.arrayToMap = function(arr) {
    var map = {};
    for (var i=0; i<arr.length; i++) {
        map[arr[i]] = 1;
    }
    return map;

};

exports.createMap = function(props) {
    var map = Object.create(null);
    for (var i in props) {
        map[i] = props[i];
    }
    return map;
};
exports.arrayRemove = function(array, value) {
  for (var i = 0; i <= array.length; i++) {
    if (value === array[i]) {
      array.splice(i, 1);
    }
  }
};

exports.escapeRegExp = function(str) {
    return str.replace(/([.*+?^${}()|[\]\/\\])/g, '\\$1');
};

exports.escapeHTML = function(str) {
    return str.replace(/&/g, "&#38;").replace(/"/g, "&#34;").replace(/'/g, "&#39;").replace(/</g, "&#60;");
};

exports.getMatchOffsets = function(string, regExp) {
    var matches = [];

    string.replace(regExp, function(str) {
        matches.push({
            offset: arguments[arguments.length-2],
            length: str.length
        });
    });

    return matches;
};
exports.deferredCall = function(fcn) {

    var timer = null;
    var callback = function() {
        timer = null;
        fcn();
    };

    var deferred = function(timeout) {
        deferred.cancel();
        timer = setTimeout(callback, timeout || 0);
        return deferred;
    };

    deferred.schedule = deferred;

    deferred.call = function() {
        this.cancel();
        fcn();
        return deferred;
    };

    deferred.cancel = function() {
        clearTimeout(timer);
        timer = null;
        return deferred;
    };

    return deferred;
};


exports.delayedCall = function(fcn, defaultTimeout) {
    var timer = null;
    var callback = function() {
        timer = null;
        fcn();
    };

    var _self = function(timeout) {
        timer && clearTimeout(timer);
        timer = setTimeout(callback, timeout || defaultTimeout);
    };

    _self.delay = _self;
    _self.schedule = function(timeout) {
        if (timer == null)
            timer = setTimeout(callback, timeout || 0);
    };

    _self.call = function() {
        this.cancel();
        fcn();
    };

    _self.cancel = function() {
        timer && clearTimeout(timer);
        timer = null;
    };

    _self.isPending = function() {
        return timer;
    };

    return _self;
};
});
ace.define('ace/mode/javascript/jshint', ['require', 'exports', 'module' ], function(require, exports, module) {
var require;
require=(function(e,t,n){function i(n,s){if(!t[n]){if(!e[n]){var o=typeof require=="function"&&require;if(!s&&o)return o(n,!0);if(r)return r(n,!0);throw new Error("Cannot find module '"+n+"'")}var u=t[n]={exports:{}};e[n][0].call(u.exports,function(t){var r=e[n][1][t];return i(r?r:t)},u,u.exports)}return t[n].exports}var r=typeof require=="function"&&require;for(var s=0;s<n.length;s++)i(n[s]);return i})({
1:[function(req,module,exports){

var process = module.exports = {};

process.nextTick = (function () {
    var canSetImmediate = typeof window !== 'undefined'
    && window.setImmediate;
    var canPost = typeof window !== 'undefined'
    && window.postMessage && window.addEventListener
    ;

    if (canSetImmediate) {
        return function (f) { return window.setImmediate(f) };
    }

    if (canPost) {
        var queue = [];
        window.addEventListener('message', function (ev) {
            if (ev.source === window && ev.data === 'process-tick') {
                ev.stopPropagation();
                if (queue.length > 0) {
                    var fn = queue.shift();
                    fn();
                }
            }
        }, true);

        return function nextTick(fn) {
            queue.push(fn);
            window.postMessage('process-tick', '*');
        };
    }

    return function nextTick(fn) {
        setTimeout(fn, 0);
    };
})();

process.title = 'browser';
process.browser = true;
process.env = {};
process.argv = [];

process.binding = function (name) {
    throw new Error('process.binding is not supported');
}
process.cwd = function () { return '/' };
process.chdir = function (dir) {
    throw new Error('process.chdir is not supported');
};

},
{}],
2:[function(req,module,exports){
(function(process){if (!process.EventEmitter) process.EventEmitter = function () {};

var EventEmitter = exports.EventEmitter = process.EventEmitter;
var isArray = typeof Array.isArray === 'function'
    ? Array.isArray
    : function (xs) {
        return Object.prototype.toString.call(xs) === '[object Array]'
    }
;
function indexOf (xs, x) {
    if (xs.indexOf) return xs.indexOf(x);
    for (var i = 0; i < xs.length; i++) {
        if (x === xs[i]) return i;
    }
    return -1;
}
var defaultMaxListeners = 200;
EventEmitter.prototype.setMaxListeners = function(n) {
  if (!this._events) this._events = {};
  this._events.maxListeners = n;
};


EventEmitter.prototype.emit = function(type) {
  if (type === 'error') {
    if (!this._events || !this._events.error ||
        (isArray(this._events.error) && !this._events.error.length))
    {
      if (arguments[1] instanceof Error) {
        throw arguments[1]; // Unhandled 'error' event
      } else {
        throw new Error("Uncaught, unspecified 'error' event.");
      }
      return false;
    }
  }

  if (!this._events) return false;
  var handler = this._events[type];
  if (!handler) return false;

  if (typeof handler == 'function') {
    switch (arguments.length) {
      case 1:
        handler.call(this);
        break;
      case 2:
        handler.call(this, arguments[1]);
        break;
      case 3:
        handler.call(this, arguments[1], arguments[2]);
        break;
      default:
        var args = Array.prototype.slice.call(arguments, 1);
        handler.apply(this, args);
    }
    return true;

  } else if (isArray(handler)) {
    var args = Array.prototype.slice.call(arguments, 1);

    var listeners = handler.slice();
    for (var i = 0, l = listeners.length; i < l; i++) {
      listeners[i].apply(this, args);
    }
    return true;

  } else {
    return false;
  }
};
EventEmitter.prototype.addListener = function(type, listener) {
  if ('function' !== typeof listener) {
    throw new Error('addListener only takes instances of Function');
  }

  if (!this._events) this._events = {};
  this.emit('newListener', type, listener);

  if (!this._events[type]) {
    this._events[type] = listener;
  } else if (isArray(this._events[type])) {
    if (!this._events[type].warned) {
      var m;
      if (this._events.maxListeners !== undefined) {
        m = this._events.maxListeners;
      } else {
        m = defaultMaxListeners;
      }

      if (m && m > 0 && this._events[type].length > m) {
        this._events[type].warned = true;
        console.error('(node) warning: possible EventEmitter memory ' +
                      'leak detected. %d listeners added. ' +
                      'Use emitter.setMaxListeners() to increase limit.',
                      this._events[type].length);
        console.trace();
      }
    }
    this._events[type].push(listener);
  } else {
    this._events[type] = [this._events[type], listener];
  }

  return this;
};

EventEmitter.prototype.on = EventEmitter.prototype.addListener;

EventEmitter.prototype.once = function(type, listener) {
  var self = this;
  self.on(type, function g() {
    self.removeListener(type, g);
    listener.apply(this, arguments);
  });

  return this;
};

EventEmitter.prototype.removeListener = function(type, listener) {
  if ('function' !== typeof listener) {
    throw new Error('removeListener only takes instances of Function');
  }
  if (!this._events || !this._events[type]) return this;

  var list = this._events[type];

  if (isArray(list)) {
    var i = indexOf(list, listener);
    if (i < 0) return this;
    list.splice(i, 1);
    if (list.length == 0)
      delete this._events[type];
  } else if (this._events[type] === listener) {
    delete this._events[type];
  }

  return this;
};

EventEmitter.prototype.removeAllListeners = function(type) {
  if (arguments.length === 0) {
    this._events = {};
    return this;
  }
  if (type && this._events && this._events[type]) this._events[type] = null;
  return this;
};

EventEmitter.prototype.listeners = function(type) {
  if (!this._events) this._events = {};
  if (!this._events[type]) this._events[type] = [];
  if (!isArray(this._events[type])) {
    this._events[type] = [this._events[type]];
  }
  return this._events[type];
};

})(req("__browserify_process"))
},
{"__browserify_process":1}],
3:[function(req,module,exports){
(function(){// jshint -W001

exports.reservedVars = {
	arguments : false,
	NaN       : false
};

exports.ecmaIdentifiers = {
	Array              : false,
	Boolean            : false,
	Date               : false,
	decodeURI          : false,
	decodeURIComponent : false,
	encodeURI          : false,
	encodeURIComponent : false,
	Error              : false,
	"eval"             : false,
	EvalError          : false,
	Function           : false,
	hasOwnProperty     : false,
	isFinite           : false,
	isNaN              : false,
	JSON               : false,
	Math               : false,
	Map                : false,
	Number             : false,
	Object             : false,
	parseInt           : false,
	parseFloat         : false,
	RangeError         : false,
	ReferenceError     : false,
	RegExp             : false,
	Set                : false,
	String             : false,
	SyntaxError        : false,
	TypeError          : false,
	URIError           : false,
	WeakMap            : false
};

exports.browser = {
	ArrayBuffer          : false,
	ArrayBufferView      : false,
	Audio                : false,
	Blob                 : false,
	addEventListener     : false,
	applicationCache     : false,
	atob                 : false,
	blur                 : false,
	btoa                 : false,
	clearInterval        : false,
	clearTimeout         : false,
	close                : false,
	closed               : false,
	DataView             : false,
	DOMParser            : false,
	defaultStatus        : false,
	document             : false,
	Element              : false,
	ElementTimeControl   : false,
	event                : false,
	FileReader           : false,
	Float32Array         : false,
	Float64Array         : false,
	FormData             : false,
	focus                : false,
	frames               : false,
	getComputedStyle     : false,
	HTMLElement          : false,
	HTMLAnchorElement    : false,
	HTMLBaseElement      : false,
	HTMLBlockquoteElement: false,
	HTMLBodyElement      : false,
	HTMLBRElement        : false,
	HTMLButtonElement    : false,
	HTMLCanvasElement    : false,
	HTMLDirectoryElement : false,
	HTMLDivElement       : false,
	HTMLDListElement     : false,
	HTMLFieldSetElement  : false,
	HTMLFontElement      : false,
	HTMLFormElement      : false,
	HTMLFrameElement     : false,
	HTMLFrameSetElement  : false,
	HTMLHeadElement      : false,
	HTMLHeadingElement   : false,
	HTMLHRElement        : false,
	HTMLHtmlElement      : false,
	HTMLIFrameElement    : false,
	HTMLImageElement     : false,
	HTMLInputElement     : false,
	HTMLIsIndexElement   : false,
	HTMLLabelElement     : false,
	HTMLLayerElement     : false,
	HTMLLegendElement    : false,
	HTMLLIElement        : false,
	HTMLLinkElement      : false,
	HTMLMapElement       : false,
	HTMLMenuElement      : false,
	HTMLMetaElement      : false,
	HTMLModElement       : false,
	HTMLObjectElement    : false,
	HTMLOListElement     : false,
	HTMLOptGroupElement  : false,
	HTMLOptionElement    : false,
	HTMLParagraphElement : false,
	HTMLParamElement     : false,
	HTMLPreElement       : false,
	HTMLQuoteElement     : false,
	HTMLScriptElement    : false,
	HTMLSelectElement    : false,
	HTMLStyleElement     : false,
	HTMLTableCaptionElement: false,
	HTMLTableCellElement : false,
	HTMLTableColElement  : false,
	HTMLTableElement     : false,
	HTMLTableRowElement  : false,
	HTMLTableSectionElement: false,
	HTMLTextAreaElement  : false,
	HTMLTitleElement     : false,
	HTMLUListElement     : false,
	HTMLVideoElement     : false,
	history              : false,
	Int16Array           : false,
	Int32Array           : false,
	Int8Array            : false,
	Image                : false,
	length               : false,
	localStorage         : false,
	location             : false,
	MessageChannel       : false,
	MessageEvent         : false,
	MessagePort          : false,
	moveBy               : false,
	moveTo               : false,
	MutationObserver     : false,
	name                 : false,
	Node                 : false,
	NodeFilter           : false,
	navigator            : false,
	onbeforeunload       : true,
	onblur               : true,
	onerror              : true,
	onfocus              : true,
	onload               : true,
	onresize             : true,
	onunload             : true,
	open                 : false,
	openDatabase         : false,
	opener               : false,
	Option               : false,
	parent               : false,
	print                : false,
	removeEventListener  : false,
	resizeBy             : false,
	resizeTo             : false,
	screen               : false,
	scroll               : false,
	scrollBy             : false,
	scrollTo             : false,
	sessionStorage       : false,
	setInterval          : false,
	setTimeout           : false,
	SharedWorker         : false,
	status               : false,
	SVGAElement          : false,
	SVGAltGlyphDefElement: false,
	SVGAltGlyphElement   : false,
	SVGAltGlyphItemElement: false,
	SVGAngle             : false,
	SVGAnimateColorElement: false,
	SVGAnimateElement    : false,
	SVGAnimateMotionElement: false,
	SVGAnimateTransformElement: false,
	SVGAnimatedAngle     : false,
	SVGAnimatedBoolean   : false,
	SVGAnimatedEnumeration: false,
	SVGAnimatedInteger   : false,
	SVGAnimatedLength    : false,
	SVGAnimatedLengthList: false,
	SVGAnimatedNumber    : false,
	SVGAnimatedNumberList: false,
	SVGAnimatedPathData  : false,
	SVGAnimatedPoints    : false,
	SVGAnimatedPreserveAspectRatio: false,
	SVGAnimatedRect      : false,
	SVGAnimatedString    : false,
	SVGAnimatedTransformList: false,
	SVGAnimationElement  : false,
	SVGCSSRule           : false,
	SVGCircleElement     : false,
	SVGClipPathElement   : false,
	SVGColor             : false,
	SVGColorProfileElement: false,
	SVGColorProfileRule  : false,
	SVGComponentTransferFunctionElement: false,
	SVGCursorElement     : false,
	SVGDefsElement       : false,
	SVGDescElement       : false,
	SVGDocument          : false,
	SVGElement           : false,
	SVGElementInstance   : false,
	SVGElementInstanceList: false,
	SVGEllipseElement    : false,
	SVGExternalResourcesRequired: false,
	SVGFEBlendElement    : false,
	SVGFEColorMatrixElement: false,
	SVGFEComponentTransferElement: false,
	SVGFECompositeElement: false,
	SVGFEConvolveMatrixElement: false,
	SVGFEDiffuseLightingElement: false,
	SVGFEDisplacementMapElement: false,
	SVGFEDistantLightElement: false,
	SVGFEFloodElement    : false,
	SVGFEFuncAElement    : false,
	SVGFEFuncBElement    : false,
	SVGFEFuncGElement    : false,
	SVGFEFuncRElement    : false,
	SVGFEGaussianBlurElement: false,
	SVGFEImageElement    : false,
	SVGFEMergeElement    : false,
	SVGFEMergeNodeElement: false,
	SVGFEMorphologyElement: false,
	SVGFEOffsetElement   : false,
	SVGFEPointLightElement: false,
	SVGFESpecularLightingElement: false,
	SVGFESpotLightElement: false,
	SVGFETileElement     : false,
	SVGFETurbulenceElement: false,
	SVGFilterElement     : false,
	SVGFilterPrimitiveStandardAttributes: false,
	SVGFitToViewBox      : false,
	SVGFontElement       : false,
	SVGFontFaceElement   : false,
	SVGFontFaceFormatElement: false,
	SVGFontFaceNameElement: false,
	SVGFontFaceSrcElement: false,
	SVGFontFaceUriElement: false,
	SVGForeignObjectElement: false,
	SVGGElement          : false,
	SVGGlyphElement      : false,
	SVGGlyphRefElement   : false,
	SVGGradientElement   : false,
	SVGHKernElement      : false,
	SVGICCColor          : false,
	SVGImageElement      : false,
	SVGLangSpace         : false,
	SVGLength            : false,
	SVGLengthList        : false,
	SVGLineElement       : false,
	SVGLinearGradientElement: false,
	SVGLocatable         : false,
	SVGMPathElement      : false,
	SVGMarkerElement     : false,
	SVGMaskElement       : false,
	SVGMatrix            : false,
	SVGMetadataElement   : false,
	SVGMissingGlyphElement: false,
	SVGNumber            : false,
	SVGNumberList        : false,
	SVGPaint             : false,
	SVGPathElement       : false,
	SVGPathSeg           : false,
	SVGPathSegArcAbs     : false,
	SVGPathSegArcRel     : false,
	SVGPathSegClosePath  : false,
	SVGPathSegCurvetoCubicAbs: false,
	SVGPathSegCurvetoCubicRel: false,
	SVGPathSegCurvetoCubicSmoothAbs: false,
	SVGPathSegCurvetoCubicSmoothRel: false,
	SVGPathSegCurvetoQuadraticAbs: false,
	SVGPathSegCurvetoQuadraticRel: false,
	SVGPathSegCurvetoQuadraticSmoothAbs: false,
	SVGPathSegCurvetoQuadraticSmoothRel: false,
	SVGPathSegLinetoAbs  : false,
	SVGPathSegLinetoHorizontalAbs: false,
	SVGPathSegLinetoHorizontalRel: false,
	SVGPathSegLinetoRel  : false,
	SVGPathSegLinetoVerticalAbs: false,
	SVGPathSegLinetoVerticalRel: false,
	SVGPathSegList       : false,
	SVGPathSegMovetoAbs  : false,
	SVGPathSegMovetoRel  : false,
	SVGPatternElement    : false,
	SVGPoint             : false,
	SVGPointList         : false,
	SVGPolygonElement    : false,
	SVGPolylineElement   : false,
	SVGPreserveAspectRatio: false,
	SVGRadialGradientElement: false,
	SVGRect              : false,
	SVGRectElement       : false,
	SVGRenderingIntent   : false,
	SVGSVGElement        : false,
	SVGScriptElement     : false,
	SVGSetElement        : false,
	SVGStopElement       : false,
	SVGStringList        : false,
	SVGStylable          : false,
	SVGStyleElement      : false,
	SVGSwitchElement     : false,
	SVGSymbolElement     : false,
	SVGTRefElement       : false,
	SVGTSpanElement      : false,
	SVGTests             : false,
	SVGTextContentElement: false,
	SVGTextElement       : false,
	SVGTextPathElement   : false,
	SVGTextPositioningElement: false,
	SVGTitleElement      : false,
	SVGTransform         : false,
	SVGTransformList     : false,
	SVGTransformable     : false,
	SVGURIReference      : false,
	SVGUnitTypes         : false,
	SVGUseElement        : false,
	SVGVKernElement      : false,
	SVGViewElement       : false,
	SVGViewSpec          : false,
	SVGZoomAndPan        : false,
	TimeEvent            : false,
	top                  : false,
	Uint16Array          : false,
	Uint32Array          : false,
	Uint8Array           : false,
	Uint8ClampedArray    : false,
	WebSocket            : false,
	window               : false,
	Worker               : false,
	XMLHttpRequest       : false,
	XMLSerializer        : false,
	XPathEvaluator       : false,
	XPathException       : false,
	XPathExpression      : false,
	XPathNamespace       : false,
	XPathNSResolver      : false,
	XPathResult          : false
};

exports.devel = {
	alert  : false,
	confirm: false,
	console: false,
	Debug  : false,
	opera  : false,
	prompt : false
};

exports.worker = {
	importScripts: true,
	postMessage  : true,
	self         : true
};
exports.nonstandard = {
	escape  : false,
	unescape: false
};

exports.couch = {
	"require" : false,
	respond   : false,
	getRow    : false,
	emit      : false,
	send      : false,
	start     : false,
	sum       : false,
	log       : false,
	exports   : false,
	module    : false,
	provides  : false
};

exports.node = {
	__filename    : false,
	__dirname     : false,
	Buffer        : false,
	DataView      : false,
	console       : false,
	exports       : true,  // In Node it is ok to exports = module.exports = foo();
	GLOBAL        : false,
	global        : false,
	module        : false,
	process       : false,
	require       : false,
	setTimeout    : false,
	clearTimeout  : false,
	setInterval   : false,
	clearInterval : false,
	setImmediate  : false, // v0.9.1+
	clearImmediate: false  // v0.9.1+
};

exports.phantom = {
	phantom      : true,
	require      : true,
	WebPage      : true
};

exports.rhino = {
	defineClass  : false,
	deserialize  : false,
	gc           : false,
	help         : false,
	importPackage: false,
	"java"       : false,
	load         : false,
	loadClass    : false,
	print        : false,
	quit         : false,
	readFile     : false,
	readUrl      : false,
	runCommand   : false,
	seal         : false,
	serialize    : false,
	spawn        : false,
	sync         : false,
	toint32      : false,
	version      : false
};

exports.shelljs = {
	target       : false,
	echo         : false,
	exit         : false,
	cd           : false,
	pwd          : false,
	ls           : false,
	find         : false,
	cp           : false,
	rm           : false,
	mv           : false,
	mkdir        : false,
	test         : false,
	cat          : false,
	sed          : false,
	grep         : false,
	which        : false,
	dirs         : false,
	pushd        : false,
	popd         : false,
	env          : false,
	exec         : false,
	chmod        : false,
	config       : false,
	error        : false,
	tempdir      : false
};

exports.wsh = {
	ActiveXObject            : true,
	Enumerator               : true,
	GetObject                : true,
	ScriptEngine             : true,
	ScriptEngineBuildVersion : true,
	ScriptEngineMajorVersion : true,
	ScriptEngineMinorVersion : true,
	VBArray                  : true,
	WSH                      : true,
	WScript                  : true,
	XDomainRequest           : true
};

exports.dojo = {
	dojo     : false,
	dijit    : false,
	dojox    : false,
	define	 : false,
	"require": false
};

exports.jquery = {
	"$"    : false,
	jQuery : false
};

exports.mootools = {
	"$"           : false,
	"$$"          : false,
	Asset         : false,
	Browser       : false,
	Chain         : false,
	Class         : false,
	Color         : false,
	Cookie        : false,
	Core          : false,
	Document      : false,
	DomReady      : false,
	DOMEvent      : false,
	DOMReady      : false,
	Drag          : false,
	Element       : false,
	Elements      : false,
	Event         : false,
	Events        : false,
	Fx            : false,
	Group         : false,
	Hash          : false,
	HtmlTable     : false,
	Iframe        : false,
	IframeShim    : false,
	InputValidator: false,
	instanceOf    : false,
	Keyboard      : false,
	Locale        : false,
	Mask          : false,
	MooTools      : false,
	Native        : false,
	Options       : false,
	OverText      : false,
	Request       : false,
	Scroller      : false,
	Slick         : false,
	Slider        : false,
	Sortables     : false,
	Spinner       : false,
	Swiff         : false,
	Tips          : false,
	Type          : false,
	typeOf        : false,
	URI           : false,
	Window        : false
};

exports.prototypejs = {
	"$"               : false,
	"$$"              : false,
	"$A"              : false,
	"$F"              : false,
	"$H"              : false,
	"$R"              : false,
	"$break"          : false,
	"$continue"       : false,
	"$w"              : false,
	Abstract          : false,
	Ajax              : false,
	Class             : false,
	Enumerable        : false,
	Element           : false,
	Event             : false,
	Field             : false,
	Form              : false,
	Hash              : false,
	Insertion         : false,
	ObjectRange       : false,
	PeriodicalExecuter: false,
	Position          : false,
	Prototype         : false,
	Selector          : false,
	Template          : false,
	Toggle            : false,
	Try               : false,
	Autocompleter     : false,
	Builder           : false,
	Control           : false,
	Draggable         : false,
	Draggables        : false,
	Droppables        : false,
	Effect            : false,
	Sortable          : false,
	SortableObserver  : false,
	Sound             : false,
	Scriptaculous     : false
};

exports.yui = {
	YUI       : false,
	Y         : false,
	YUI_config: false
};


})()
},
{}],
4:[function(req,module,exports){

"use string";
exports.unsafeString =
	/@cc|<\/?|script|\]\s*\]|<\s*!|&lt/i;
exports.unsafeChars =
	/[\u0000-\u001f\u007f-\u009f\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/;
exports.needEsc =
	/[\u0000-\u001f&<"\/\\\u007f-\u009f\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/;

exports.needEscGlobal =
	/[\u0000-\u001f&<"\/\\\u007f-\u009f\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g;
exports.starSlash = /\*\//;
exports.identifier = /^([a-zA-Z_$][a-zA-Z0-9_$]*)$/;
exports.javascriptURL = /^(?:javascript|jscript|ecmascript|vbscript|mocha|livescript)\s*:/i;
exports.fallsThrough = /^\s*\/\*\s*falls?\sthrough\s*\*\/\s*$/;

},
{}],
5:[function(req,module,exports){


var state = {
	syntax: {},

	reset: function () {
		this.tokens = {
			prev: null,
			next: null,
			curr: null
		};

		this.option = {};
		this.ignored = {};
		this.directive = {};
		this.jsonMode = false;
		this.jsonWarnings = [];
		this.lines = [];
		this.tab = "";
		this.cache = {}; // Node.JS doesn't have Map. Sniff.
	}
};

exports.state = state;

},
{}],
6:[function(req,module,exports){
(function(){

exports.register = function (linter) {

	linter.on("Identifier", function style_scanProto(data) {
		if (linter.getOption("proto")) {
			return;
		}

		if (data.name === "__proto__") {
			linter.warn("W103", {
				line: data.line,
				char: data.char,
				data: [ data.name ]
			});
		}
	});

	linter.on("Identifier", function style_scanIterator(data) {
		if (linter.getOption("iterator")) {
			return;
		}

		if (data.name === "__iterator__") {
			linter.warn("W104", {
				line: data.line,
				char: data.char,
				data: [ data.name ]
			});
		}
	});

	linter.on("Identifier", function style_scanDangling(data) {
		if (!linter.getOption("nomen")) {
			return;
		}
		if (data.name === "_") {
			return;
		}
		if (linter.getOption("node")) {
			if (/^(__dirname|__filename)$/.test(data.name) && !data.isProperty) {
				return;
			}
		}

		if (/^(_+.*|.*_+)$/.test(data.name)) {
			linter.warn("W105", {
				line: data.line,
				char: data.from,
				data: [ "dangling '_'", data.name ]
			});
		}
	});

	linter.on("Identifier", function style_scanCamelCase(data) {
		if (!linter.getOption("camelcase")) {
			return;
		}

		if (data.name.replace(/^_+/, "").indexOf("_") > -1 && !data.name.match(/^[A-Z0-9_]*$/)) {
			linter.warn("W106", {
				line: data.line,
				char: data.from,
				data: [ data.name ]
			});
		}
	});

	linter.on("String", function style_scanQuotes(data) {
		var quotmark = linter.getOption("quotmark");
		var code;

		if (!quotmark) {
			return;
		}

		if (quotmark === "single" && data.quote !== "'") {
			code = "W109";
		}

		if (quotmark === "double" && data.quote !== "\"") {
			code = "W108";
		}

		if (quotmark === true) {
			if (!linter.getCache("quotmark")) {
				linter.setCache("quotmark", data.quote);
			}

			if (linter.getCache("quotmark") !== data.quote) {
				code = "W110";
			}
		}

		if (code) {
			linter.warn(code, {
				line: data.line,
				char: data.char,
			});
		}
	});

	linter.on("Number", function style_scanNumbers(data) {
		if (data.value.charAt(0) === ".") {
			linter.warn("W008", {
				line: data.line,
				char: data.char,
				data: [ data.value ]
			});
		}

		if (data.value.substr(data.value.length - 1) === ".") {
			linter.warn("W047", {
				line: data.line,
				char: data.char,
				data: [ data.value ]
			});
		}

		if (/^00+/.test(data.value)) {
			linter.warn("W046", {
				line: data.line,
				char: data.char,
				data: [ data.value ]
			});
		}
	});

	linter.on("String", function style_scanJavaScriptURLs(data) {
		var re = /^(?:javascript|jscript|ecmascript|vbscript|mocha|livescript)\s*:/i;

		if (linter.getOption("scripturl")) {
			return;
		}

		if (re.test(data.value)) {
			linter.warn("W107", {
				line: data.line,
				char: data.char
			});
		}
	});
};
})()
},
{}],
7:[function(req,module,exports){
(function(global){/*global window, global*/
var functions = [
    [log, "log"]
    , [info, "info"]
    , [warn, "warn"]
    , [error, "error"]
    , [time, "time"]
    , [timeEnd, "timeEnd"]
    , [trace, "trace"]
    , [dir, "dir"]
    , [assert, "assert"]
]

for (var i = 0; i < functions.length; i++) {
    var tuple = functions[i]
    var f = tuple[0]
    var name = tuple[1]

    if (!console[name]) {
        console[name] = f
    }
}

module.exports = console

function log() {}

function info() {}

function warn() {}

function error() {}

function time(label) {}

function timeEnd(label) {}

function trace() {}

function dir(object) {}

function assert(expression) {}

})(window)
},
{}],
"jshint":[function(req,module,exports){
module.exports=req('E/GbHF');
},
{}],"E/GbHF":[function(req,module,exports){
(function(){/*!
 * JSLINT, by Douglas Crockford.
 *
 * Permission is hereby granted, free of charge, to any person obtaining 
 * a copy of this software and associated documentation files (the 
 * "Software"), to deal in the Software without restriction, including 
 * without limitation the rights to use, copy, modify, merge, publish, 
 * distribute, sublicense, and/or sell copies of the Software, and to 
 * permit persons to whom the Software is furnished to do so, subject to 
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be 
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY 
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, 
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 *
 */

var _        = req("underscore");
var events   = req("events");
var vars     = req("../shared/vars.js");
var messages = req("../shared/messages.js");
var Lexer    = req("./lex.js").Lexer;
var reg      = req("./reg.js");
var state    = req("./state.js").state;
var style    = req("./style.js");
var console = req("console-browserify");


var JSLINT = (function () {
    'use strict';

    function array_to_object(array, value) {

// Make an object from an array of keys and a common value.

        var i, length = array.length, object = {};
        for (i = 0; i < length; i += 1) {
            object[array[i]] = value;
        }
        return object;
    }


    var adsafe_id,      // The widget's ADsafe id.
        adsafe_may,     // The widget may load approved scripts.
        adsafe_top,     // At the top of the widget script.
        adsafe_went,    // ADSAFE.go has been called.
        allowed_option = {
            bitwise   : true,
            browser   : true,
            cap       : true,
            confusion : true,
            'continue': true,
            css       : true,
            debug     : true,
            devel     : true,
            eqeq      : true,
            es5       : true,
            evil      : true,
            forin     : true,
            fragment  : true,
            indent    :   10,
            maxerr    : 1000,
            maxlen    :  256,
            newcap    : true,
            node      : true,
            nomen     : true,
            on        : true,
            passfail  : true,
            plusplus  : true,
            properties: true,
            regexp    : true,
            rhino     : true,
            undef     : true,
            unparam   : true,
            sloppy    : true,
            sub       : true,
            vars      : true,
            white     : true,
            widget    : true,
            windows   : true
        },
        anonname,       // The guessed name for anonymous functions.
        approved,       // ADsafe approved urls.

// These are operators that should not be used with the ! operator.

        bang = {
            '<'  : true,
            '<=' : true,
            '==' : true,
            '===': true,
            '!==': true,
            '!=' : true,
            '>'  : true,
            '>=' : true,
            '+'  : true,
            '-'  : true,
            '*'  : true,
            '/'  : true,
            '%'  : true
        },

// These are property names that should not be permitted in the safe subset.

        banned = array_to_object([
            'arguments', 'callee', 'caller', 'constructor', 'eval', 'prototype',
            'stack', 'unwatch', 'valueOf', 'watch'
        ], true),
        begin,          // The root token

// browser contains a set of global names that are commonly provided by a
// web browser environment.

        browser = array_to_object([
            'clearInterval', 'clearTimeout', 'document', 'event', 'frames',
            'history', 'Image', 'localStorage', 'location', 'name', 'navigator',
            'Option', 'parent', 'screen', 'sessionStorage', 'setInterval',
            'setTimeout', 'Storage', 'window', 'XMLHttpRequest'
        ], false),

// bundle contains the text messages.

        bundle = {
            a_label: "'{a}' is a statement label.",
            a_not_allowed: "'{a}' is not allowed.",
            a_not_defined: "'{a}' is not defined.",
            a_scope: "'{a}' used out of scope.",
            adsafe_a: "ADsafe violation: '{a}'.",
            adsafe_autocomplete: "ADsafe autocomplete violation.",
            adsafe_bad_id: "ADSAFE violation: bad id.",
            adsafe_div: "ADsafe violation: Wrap the widget in a div.",
            adsafe_fragment: "ADSAFE: Use the fragment option.",
            adsafe_go: "ADsafe violation: Misformed ADSAFE.go.",
            adsafe_html: "Currently, ADsafe does not operate on whole HTML " +
                "documents. It operates on <div> fragments and .js files.",
            adsafe_id: "ADsafe violation: id does not match.",
            adsafe_id_go: "ADsafe violation: Missing ADSAFE.id or ADSAFE.go.",
            adsafe_lib: "ADsafe lib violation.",
            adsafe_lib_second: "ADsafe: The second argument to lib must be a function.",
            adsafe_missing_id: "ADSAFE violation: missing ID_.",
            adsafe_name_a: "ADsafe name violation: '{a}'.",
            adsafe_placement: "ADsafe script placement violation.",
            adsafe_prefix_a: "ADsafe violation: An id must have a '{a}' prefix",
            adsafe_script: "ADsafe script violation.",
            adsafe_source: "ADsafe unapproved script source.",
            adsafe_subscript_a: "ADsafe subscript '{a}'.",
            adsafe_tag: "ADsafe violation: Disallowed tag '{a}'.",
            already_defined: "'{a}' is already defined.",
            and: "The '&&' subexpression should be wrapped in parens.",
            assign_exception: "Do not assign to the exception parameter.",
            assignment_function_expression: "Expected an assignment or " +
                "function call and instead saw an expression.",
            attribute_case_a: "Attribute '{a}' not all lower case.",
            avoid_a: "Avoid '{a}'.",
            bad_assignment: "Bad assignment.",
            bad_color_a: "Bad hex color '{a}'.",
            bad_constructor: "Bad constructor.",
            bad_entity: "Bad entity.",
            bad_html: "Bad HTML string",
            bad_id_a: "Bad id: '{a}'.",
            bad_in_a: "Bad for in variable '{a}'.",
            bad_invocation: "Bad invocation.",
            bad_name_a: "Bad name: '{a}'.",
            bad_new: "Do not use 'new' for side effects.",
            bad_number: "Bad number '{a}'.",
            bad_operand: "Bad operand.",
            bad_style: "Bad style.",
            bad_type: "Bad type.",
            bad_url_a: "Bad url '{a}'.",
            bad_wrap: "Do not wrap function literals in parens unless they " +
                "are to be immediately invoked.",
            combine_var: "Combine this with the previous 'var' statement.",
            conditional_assignment: "Expected a conditional expression and " +
                "instead saw an assignment.",
            confusing_a: "Confusing use of '{a}'.",
            confusing_regexp: "Confusing regular expression.",
            constructor_name_a: "A constructor name '{a}' should start with " +
                "an uppercase letter.",
            control_a: "Unexpected control character '{a}'.",
            css: "A css file should begin with @charset 'UTF-8';",
            dangling_a: "Unexpected dangling '_' in '{a}'.",
            dangerous_comment: "Dangerous comment.",
            deleted: "Only properties should be deleted.",
            duplicate_a: "Duplicate '{a}'.",
            empty_block: "Empty block.",
            empty_case: "Empty case.",
            empty_class: "Empty class.",
            es5: "This is an ES5 feature.",
            evil: "eval is evil.",
            expected_a: "Expected '{a}'.",
            expected_a_b: "Expected '{a}' and instead saw '{b}'.",
            expected_a_b_from_c_d: "Expected '{a}' to match '{b}' from line " +
                "{c} and instead saw '{d}'.",
            expected_at_a: "Expected an at-rule, and instead saw @{a}.",
            expected_a_at_b_c: "Expected '{a}' at column {b}, not column {c}.",
            expected_attribute_a: "Expected an attribute, and instead saw [{a}].",
            expected_attribute_value_a: "Expected an attribute value and " +
                "instead saw '{a}'.",
            expected_class_a: "Expected a class, and instead saw .{a}.",
            expected_fraction_a: "Expected a number between 0 and 1 and " +
                "instead saw '{a}'",
            expected_id_a: "Expected an id, and instead saw #{a}.",
            expected_identifier_a: "Expected an identifier and instead saw '{a}'.",
            expected_identifier_a_reserved: "Expected an identifier and " +
                "instead saw '{a}' (a reserved word).",
            expected_linear_a: "Expected a linear unit and instead saw '{a}'.",
            expected_lang_a: "Expected a lang code, and instead saw :{a}.",
            expected_media_a: "Expected a CSS media type, and instead saw '{a}'.",
            expected_name_a: "Expected a name and instead saw '{a}'.",
            expected_nonstandard_style_attribute: "Expected a non-standard " +
                "style attribute and instead saw '{a}'.",
            expected_number_a: "Expected a number and instead saw '{a}'.",
            expected_operator_a: "Expected an operator and instead saw '{a}'.",
            expected_percent_a: "Expected a percentage and instead saw '{a}'",
            expected_positive_a: "Expected a positive number and instead saw '{a}'",
            expected_pseudo_a: "Expected a pseudo, and instead saw :{a}.",
            expected_selector_a: "Expected a CSS selector, and instead saw {a}.",
            expected_small_a: "Expected a small positive integer and instead saw '{a}'",
            expected_space_a_b: "Expected exactly one space between '{a}' and '{b}'.",
            expected_string_a: "Expected a string and instead saw {a}.",
            expected_style_attribute: "Excepted a style attribute, and instead saw '{a}'.",
            expected_style_pattern: "Expected a style pattern, and instead saw '{a}'.",
            expected_tagname_a: "Expected a tagName, and instead saw {a}.",
            expected_type_a: "Expected a type, and instead saw {a}.",
            for_if: "The body of a for in should be wrapped in an if " +
                "statement to filter unwanted properties from the prototype.",
            function_block: "Function statements should not be placed in blocks. " +
                "Use a function expression or move the statement to the top of " +
                "the outer function.",
            function_eval: "The Function constructor is eval.",
            function_loop: "Don't make functions within a loop.",
            function_statement: "Function statements are not invocable. " +
                "Wrap the whole function invocation in parens.",
            function_strict: "Use the function form of 'use strict'.",
            html_confusion_a: "HTML confusion in regular expression '<{a}'.",
            html_handlers: "Avoid HTML event handlers.",
            identifier_function: "Expected an identifier in an assignment " +
                "and instead saw a function invocation.",
            implied_evil: "Implied eval is evil. Pass a function instead of a string.",
            infix_in: "Unexpected 'in'. Compare with undefined, or use the " +
                "hasOwnProperty method instead.",
            insecure_a: "Insecure '{a}'.",
            isNaN: "Use the isNaN function to compare with NaN.",
            label_a_b: "Label '{a}' on '{b}' statement.",
            lang: "lang is deprecated.",
            leading_decimal_a: "A leading decimal point can be confused with a dot: '.{a}'.",
            missing_a: "Missing '{a}'.",
            missing_a_after_b: "Missing '{a}' after '{b}'.",
            missing_option: "Missing option value.",
            missing_property: "Missing property name.",
            missing_space_a_b: "Missing space between '{a}' and '{b}'.",
            missing_url: "Missing url.",
            missing_use_strict: "Missing 'use strict' statement.",
            mixed: "Mixed spaces and tabs.",
            move_invocation: "Move the invocation into the parens that " +
                "contain the function.",
            move_var: "Move 'var' declarations to the top of the function.",
            name_function: "Missing name in function statement.",
            nested_comment: "Nested comment.",
            not: "Nested not.",
            not_a_constructor: "Do not use {a} as a constructor.",
            not_a_defined: "'{a}' has not been fully defined yet.",
            not_a_function: "'{a}' is not a function.",
            not_a_label: "'{a}' is not a label.",
            not_a_scope: "'{a}' is out of scope.",
            not_greater: "'{a}' should not be greater than '{b}'.",
            parameter_a_get_b: "Unexpected parameter '{a}' in get {b} function.",
            parameter_set_a: "Expected parameter (value) in set {a} function.",
            radix: "Missing radix parameter.",
            read_only: "Read only.",
            redefinition_a: "Redefinition of '{a}'.",
            reserved_a: "Reserved name '{a}'.",
            scanned_a_b: "{a} ({b}% scanned).",
            slash_equal: "A regular expression literal can be confused with '/='.",
            statement_block: "Expected to see a statement and instead saw a block.",
            stopping: "Stopping. ",
            strange_loop: "Strange loop.",
            strict: "Strict violation.",
            subscript: "['{a}'] is better written in dot notation.",
            tag_a_in_b: "A '<{a}>' must be within '<{b}>'.",
            too_long: "Line too long.",
            too_many: "Too many errors.",
            trailing_decimal_a: "A trailing decimal point can be confused " +
                "with a dot: '.{a}'.",
            type: "type is unnecessary.",
            type_confusion_a_b: "Type confusion: {a} and {b}.",
            unclosed: "Unclosed string.",
            unclosed_comment: "Unclosed comment.",
            unclosed_regexp: "Unclosed regular expression.",
            unescaped_a: "Unescaped '{a}'.",
            unexpected_a: "Unexpected '{a}'.",
            unexpected_char_a_b: "Unexpected character '{a}' in {b}.",
            unexpected_comment: "Unexpected comment.",
            unexpected_property_a: "Unexpected /*property*/ '{a}'.",
            unexpected_space_a_b: "Unexpected space between '{a}' and '{b}'.",
            unnecessary_initialize: "It is not necessary to initialize '{a}' " +
                "to 'undefined'.",
            unnecessary_use: "Unnecessary 'use strict'.",
            unreachable_a_b: "Unreachable '{a}' after '{b}'.",
            unrecognized_style_attribute_a: "Unrecognized style attribute '{a}'.",
            unrecognized_tag_a: "Unrecognized tag '<{a}>'.",
            unsafe: "Unsafe character.",
            url: "JavaScript URL.",
            use_array: "Use the array literal notation [].",
            use_braces: "Spaces are hard to count. Use {{a}}.",
            use_charAt: "Use the charAt method.",
            use_object: "Use the object literal notation {}.",
            use_or: "Use the || operator.",
            use_param: "Use a named parameter.",
            used_before_a: "'{a}' was used before it was defined.",
            var_a_not: "Variable {a} was not declared correctly.",
            weird_assignment: "Weird assignment.",
            weird_condition: "Weird condition.",
            weird_new: "Weird construction. Delete 'new'.",
            weird_program: "Weird program.",
            weird_relation: "Weird relation.",
            weird_ternary: "Weird ternary.",
            wrap_immediate: "Wrap an immediate function invocation in parentheses " +
                "to assist the reader in understanding that the expression " +
                "is the result of a function, and not the function itself.",
            wrap_regexp: "Wrap the /regexp/ literal in parens to " +
                "disambiguate the slash operator.",
            write_is_wrong: "document.write can be a form of eval."
        },
        comments_off,
        css_attribute_data,
        css_any,

        css_colorData = array_to_object([
            "aliceblue", "antiquewhite", "aqua", "aquamarine", "azure", "beige",
            "bisque", "black", "blanchedalmond", "blue", "blueviolet", "brown",
            "burlywood", "cadetblue", "chartreuse", "chocolate", "coral",
            "cornflowerblue", "cornsilk", "crimson", "cyan", "darkblue",
            "darkcyan", "darkgoldenrod", "darkgray", "darkgreen", "darkkhaki",
            "darkmagenta", "darkolivegreen", "darkorange", "darkorchid",
            "darkred", "darksalmon", "darkseagreen", "darkslateblue",
            "darkslategray", "darkturquoise", "darkviolet", "deeppink",
            "deepskyblue", "dimgray", "dodgerblue", "firebrick", "floralwhite",
            "forestgreen", "fuchsia", "gainsboro", "ghostwhite", "gold",
            "goldenrod", "gray", "green", "greenyellow", "honeydew", "hotpink",
            "indianred", "indigo", "ivory", "khaki", "lavender",
            "lavenderblush", "lawngreen", "lemonchiffon", "lightblue",
            "lightcoral", "lightcyan", "lightgoldenrodyellow", "lightgreen",
            "lightpink", "lightsalmon", "lightseagreen", "lightskyblue",
            "lightslategray", "lightsteelblue", "lightyellow", "lime",
            "limegreen", "linen", "magenta", "maroon", "mediumaquamarine",
            "mediumblue", "mediumorchid", "mediumpurple", "mediumseagreen",
            "mediumslateblue", "mediumspringgreen", "mediumturquoise",
            "mediumvioletred", "midnightblue", "mintcream", "mistyrose",
            "moccasin", "navajowhite", "navy", "oldlace", "olive", "olivedrab",
            "orange", "orangered", "orchid", "palegoldenrod", "palegreen",
            "paleturquoise", "palevioletred", "papayawhip", "peachpuff",
            "peru", "pink", "plum", "powderblue", "purple", "red", "rosybrown",
            "royalblue", "saddlebrown", "salmon", "sandybrown", "seagreen",
            "seashell", "sienna", "silver", "skyblue", "slateblue", "slategray",
            "snow", "springgreen", "steelblue", "tan", "teal", "thistle",
            "tomato", "turquoise", "violet", "wheat", "white", "whitesmoke",
            "yellow", "yellowgreen",

            "activeborder", "activecaption", "appworkspace", "background",
            "buttonface", "buttonhighlight", "buttonshadow", "buttontext",
            "captiontext", "graytext", "highlight", "highlighttext",
            "inactiveborder", "inactivecaption", "inactivecaptiontext",
            "infobackground", "infotext", "menu", "menutext", "scrollbar",
            "threeddarkshadow", "threedface", "threedhighlight",
            "threedlightshadow", "threedshadow", "window", "windowframe",
            "windowtext"
        ], true),

        css_border_style,
        css_break,

        css_lengthData = {
            '%': true,
            'cm': true,
            'em': true,
            'ex': true,
            'in': true,
            'mm': true,
            'pc': true,
            'pt': true,
            'px': true
        },

        css_media,
        css_overflow,

        descapes = {
            'b': '\b',
            't': '\t',
            'n': '\n',
            'f': '\f',
            'r': '\r',
            '"': '"',
            '/': '/',
            '\\': '\\'
        },

        devel = array_to_object([
            'alert', 'confirm', 'console', 'Debug', 'opera', 'prompt', 'WSH'
        ], false),
        directive,
        escapes = {
            '\b': '\\b',
            '\t': '\\t',
            '\n': '\\n',
            '\f': '\\f',
            '\r': '\\r',
            '\'': '\\\'',
            '"' : '\\"',
            '/' : '\\/',
            '\\': '\\\\'
        },

        funct,          // The current function, including the labels used in
                        // the function, as well as (breakage),
                        // (context), (loopage), (name), (params), (token),
                        // (vars), (verb)

        functionicity = [
            'closure', 'exception', 'global', 'label', 'outer', 'undef',
            'unused', 'var'
        ],

        functions,      // All of the functions
        global_funct,   // The global body
        global_scope,   // The global scope
        html_tag = {
            a:        {},
            abbr:     {},
            acronym:  {},
            address:  {},
            applet:   {},
            area:     {empty: true, parent: ' map '},
            article:  {},
            aside:    {},
            audio:    {},
            b:        {},
            base:     {empty: true, parent: ' head '},
            bdo:      {},
            big:      {},
            blockquote: {},
            body:     {parent: ' html noframes '},
            br:       {empty: true},
            button:   {},
            canvas:   {parent: ' body p div th td '},
            caption:  {parent: ' table '},
            center:   {},
            cite:     {},
            code:     {},
            col:      {empty: true, parent: ' table colgroup '},
            colgroup: {parent: ' table '},
            command:  {parent: ' menu '},
            datalist: {},
            dd:       {parent: ' dl '},
            del:      {},
            details:  {},
            dialog:   {},
            dfn:      {},
            dir:      {},
            div:      {},
            dl:       {},
            dt:       {parent: ' dl '},
            em:       {},
            embed:    {},
            fieldset: {},
            figure:   {},
            font:     {},
            footer:   {},
            form:     {},
            frame:    {empty: true, parent: ' frameset '},
            frameset: {parent: ' html frameset '},
            h1:       {},
            h2:       {},
            h3:       {},
            h4:       {},
            h5:       {},
            h6:       {},
            head:     {parent: ' html '},
            header:   {},
            hgroup:   {},
            hr:       {empty: true},
            'hta:application':
                      {empty: true, parent: ' head '},
            html:     {parent: '*'},
            i:        {},
            iframe:   {},
            img:      {empty: true},
            input:    {empty: true},
            ins:      {},
            kbd:      {},
            keygen:   {},
            label:    {},
            legend:   {parent: ' details fieldset figure '},
            li:       {parent: ' dir menu ol ul '},
            link:     {empty: true, parent: ' head '},
            map:      {},
            mark:     {},
            menu:     {},
            meta:     {empty: true, parent: ' head noframes noscript '},
            meter:    {},
            nav:      {},
            noframes: {parent: ' html body '},
            noscript: {parent: ' body head noframes '},
            object:   {},
            ol:       {},
            optgroup: {parent: ' select '},
            option:   {parent: ' optgroup select '},
            output:   {},
            p:        {},
            param:    {empty: true, parent: ' applet object '},
            pre:      {},
            progress: {},
            q:        {},
            rp:       {},
            rt:       {},
            ruby:     {},
            samp:     {},
            script:   {empty: true, parent: ' body div frame head iframe p pre span '},
            section:  {},
            select:   {},
            small:    {},
            span:     {},
            source:   {},
            strong:   {},
            style:    {parent: ' head ', empty: true},
            sub:      {},
            sup:      {},
            table:    {},
            tbody:    {parent: ' table '},
            td:       {parent: ' tr '},
            textarea: {},
            tfoot:    {parent: ' table '},
            th:       {parent: ' tr '},
            thead:    {parent: ' table '},
            time:     {},
            title:    {parent: ' head '},
            tr:       {parent: ' table tbody thead tfoot '},
            tt:       {},
            u:        {},
            ul:       {},
            'var':    {},
            video:    {}
        },

        ids,            // HTML ids
        in_block,
        indent,
//         infer_statement,// Inference rules for statements
        is_type = array_to_object([
            '*', 'array', 'boolean', 'function', 'number', 'object',
            'regexp', 'string'
        ], true),
        itself,         // JSLint itself
        json_mode,
        lex,            // the tokenizer
        lines,
        lookahead,
        member,
        node = array_to_object([
            'Buffer', 'clearInterval', 'clearTimeout', 'console', 'exports',
            'global', 'module', 'process', 'querystring', 'require',
            'setInterval', 'setTimeout', '__dirname', '__filename'
        ], false),
        node_js,
        numbery = array_to_object(['indexOf', 'lastIndexOf', 'search'], true),
        next_token,
        option,
        predefined,     // Global variables defined by option
        prereg,
        prev_token,
        property_type,
        regexp_flag = array_to_object(['g', 'i', 'm'], true),
        return_this = function return_this() {
            return this;
        },
        rhino = array_to_object([
            'defineClass', 'deserialize', 'gc', 'help', 'load', 'loadClass',
            'print', 'quit', 'readFile', 'readUrl', 'runCommand', 'seal',
            'serialize', 'spawn', 'sync', 'toint32', 'version'
        ], false),

        scope,      // An object containing an object for each variable in scope
        semicolon_coda = array_to_object([';', '"', '\'', ')'], true),
        src,
        stack,

// standard contains the global names that are provided by the
// ECMAScript standard.

        standard = array_to_object([
            'Array', 'Boolean', 'Date', 'decodeURI', 'decodeURIComponent',
            'encodeURI', 'encodeURIComponent', 'Error', 'eval', 'EvalError',
            'Function', 'isFinite', 'isNaN', 'JSON', 'Math', 'Number', 'Object',
            'parseInt', 'parseFloat', 'RangeError', 'ReferenceError', 'RegExp',
            'String', 'SyntaxError', 'TypeError', 'URIError'
        ], false),

        standard_property_type = {
            E                   : 'number',
            LN2                 : 'number',
            LN10                : 'number',
            LOG2E               : 'number',
            LOG10E              : 'number',
            MAX_VALUE           : 'number',
            MIN_VALUE           : 'number',
            NEGATIVE_INFINITY   : 'number',
            PI                  : 'number',
            POSITIVE_INFINITY   : 'number',
            SQRT1_2             : 'number',
            SQRT2               : 'number',
            apply               : 'function',
            bind                : 'function function',
            call                : 'function',
            ceil                : 'function number',
            charAt              : 'function string',
            concat              : 'function',
            constructor         : 'function object',
            create              : 'function object',
            defineProperty      : 'function object',
            defineProperties    : 'function object',
            every               : 'function boolean',
            exec                : 'function array',
            filter              : 'function array',
            floor               : 'function number',
            forEach             : 'function',
            freeze              : 'function object',
            getDate             : 'function number',
            getDay              : 'function number',
            getFullYear         : 'function number',
            getHours            : 'function number',
            getMilliseconds     : 'function number',
            getMinutes          : 'function number',
            getMonth            : 'function number',
            getOwnPropertyDescriptor
                                : 'function object',
            getOwnPropertyNames : 'function array',
            getPrototypeOf      : 'function object',
            getSeconds          : 'function number',
            getTime             : 'function number',
            getTimezoneOffset   : 'function number',
            getUTCDate          : 'function number',
            getUTCDay           : 'function number',
            getUTCFullYear      : 'function number',
            getUTCHours         : 'function number',
            getUTCMilliseconds  : 'function number',
            getUTCMinutes       : 'function number',
            getUTCMonth         : 'function number',
            getUTCSeconds       : 'function number',
            getYear             : 'function number',
            hasOwnProperty      : 'function boolean',
            indexOf             : 'function number',
            isExtensible        : 'function boolean',
            isFrozen            : 'function boolean',
            isPrototypeOf       : 'function boolean',
            isSealed            : 'function boolean',
            join                : 'function string',
            keys                : 'function array',
            lastIndexOf         : 'function number',
            lastIndex           : 'number',
            length              : 'number',
            map                 : 'function array',
            now                 : 'function number',
            parse               : 'function',
            pop                 : 'function',
            preventExtensions   : 'function object',
            propertyIsEnumerable: 'function boolean',
            prototype           : 'object',
            push                : 'function number',
            reduce              : 'function',
            reduceRight         : 'function',
            reverse             : 'function',
            seal                : 'function object',
            setDate             : 'function',
            setDay              : 'function',
            setFullYear         : 'function',
            setHours            : 'function',
            setMilliseconds     : 'function',
            setMinutes          : 'function',
            setMonth            : 'function',
            setSeconds          : 'function',
            setTime             : 'function',
            setTimezoneOffset   : 'function',
            setUTCDate          : 'function',
            setUTCDay           : 'function',
            setUTCFullYear      : 'function',
            setUTCHours         : 'function',
            setUTCMilliseconds  : 'function',
            setUTCMinutes       : 'function',
            setUTCMonth         : 'function',
            setUTCSeconds       : 'function',
            setYear             : 'function',
            shift               : 'function',
            slice               : 'function',
            some                : 'function boolean',
            sort                : 'function',
            splice              : 'function',
            stringify           : 'function string',
            substr              : 'function string',
            substring           : 'function string',
            test                : 'function boolean',
            toDateString        : 'function string',
            toExponential       : 'function string',
            toFixed             : 'function string',
            toJSON              : 'function',
            toISOString         : 'function string',
            toLocaleDateString  : 'function string',
            toLocaleLowerCase   : 'function string',
            toLocaleUpperCase   : 'function string',
            toLocaleString      : 'function string',
            toLocaleTimeString  : 'function string',
            toLowerCase         : 'function string',
            toPrecision         : 'function string',
            toTimeString        : 'function string',
            toUpperCase         : 'function string',
            toUTCString         : 'function string',
            trim                : 'function string',
            unshift             : 'function number',
            valueOf             : 'function'
        },

        strict_mode,
        syntax = {},
        tab,
        token,
//         type_state_change,
        urls,
        var_mode,
        warnings,

// widget contains the global names which are provided to a Yahoo
// (fna Konfabulator) widget.

        widget = array_to_object([
            'alert', 'animator', 'appleScript', 'beep', 'bytesToUIString',
            'Canvas', 'chooseColor', 'chooseFile', 'chooseFolder',
            'closeWidget', 'COM', 'convertPathToHFS', 'convertPathToPlatform',
            'CustomAnimation', 'escape', 'FadeAnimation', 'filesystem', 'Flash',
            'focusWidget', 'form', 'FormField', 'Frame', 'HotKey', 'Image',
            'include', 'isApplicationRunning', 'iTunes', 'konfabulatorVersion',
            'log', 'md5', 'MenuItem', 'MoveAnimation', 'openURL', 'play',
            'Point', 'popupMenu', 'preferenceGroups', 'preferences', 'print',
            'prompt', 'random', 'Rectangle', 'reloadWidget', 'ResizeAnimation',
            'resolvePath', 'resumeUpdates', 'RotateAnimation', 'runCommand',
            'runCommandInBg', 'saveAs', 'savePreferences', 'screen',
            'ScrollBar', 'showWidgetPreferences', 'sleep', 'speak', 'Style',
            'suppressUpdates', 'system', 'tellWidget', 'Text', 'TextArea',
            'Timer', 'unescape', 'updateNow', 'URL', 'Web', 'widget', 'Window',
            'XMLDOM', 'XMLHttpRequest', 'yahooCheckLogin', 'yahooLogin',
            'yahooLogout'
        ], true),

        windows = array_to_object([
            'ActiveXObject', 'CScript', 'Debug', 'Enumerator', 'System',
            'VBArray', 'WScript', 'WSH'
        ], false),

//  xmode is used to adapt to the exceptions in html parsing.
//  It can have these states:
//      ''      .js script file
//      'html'
//      'outer'
//      'script'
//      'style'
//      'scriptstring'
//      'styleproperty'

        xmode,
        xquote,

// Regular expressions. Some of these are stupidly long.

// unsafe comment or string
        ax = /@cc|<\/?|script|\]\s*\]|<\s*!|&lt/i,
// carriage return, or carriage return linefeed
        crx = /\r/g,
        crlfx = /\r\n/g,
// unsafe characters that are silently deleted by one or more browsers
        cx = /[\u0000-\u001f\u007f-\u009f\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/,
// query characters for ids
        dx = /[\[\]\/\\"'*<>.&:(){}+=#]/,
// html token
        hx = /^\s*(['"=>\/&#]|<(?:\/|\!(?:--)?)?|[a-zA-Z][a-zA-Z0-9_\-:]*|[0-9]+|--)/,
// identifier
        ix = /^([a-zA-Z_$][a-zA-Z0-9_$]*)$/,
// javascript url
        jx = /^(?:javascript|jscript|ecmascript|vbscript|mocha|livescript)\s*:/i,
// star slash
        lx = /\*\/|\/\*/,
// characters in strings that need escapement
        nx = /[\u0000-\u001f'\\\u007f-\u009f\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g,
// outer html token
        ox = /[>&]|<[\/!]?|--/,
// attributes characters
        qx = /[^a-zA-Z0-9+\-_\/ ]/,
// style
        sx = /^\s*([{}:#%.=,>+\[\]@()"';]|[*$\^~]=|[a-zA-Z_][a-zA-Z0-9_\-]*|[0-9]+|<\/|\/\*)/,
        ssx = /^\s*([@#!"'};:\-%.=,+\[\]()*_]|[a-zA-Z][a-zA-Z0-9._\-]*|\/\*?|\d+(?:\.\d+)?|<\/)/,
// token
        tx = /^\s*([(){}\[\]\?.,:;'"~#@`]|={1,3}|\/(\*(jslint|properties|property|members?|globals?)?|=|\/)?|\*[\/=]?|\+(?:=|\++)?|-(?:=|-+)?|[\^%]=?|&[&=]?|\|[|=]?|>{1,3}=?|<(?:[\/=!]|\!(\[|--)?|<=?)?|\!={0,2}|[a-zA-Z_$][a-zA-Z0-9_$]*|[0-9]+(?:[xX][0-9a-fA-F]+|\.[0-9]*)?(?:[eE][+\-]?[0-9]+)?)/,
// url badness
        ux = /&|\+|\u00AD|\.\.|\/\*|%[^;]|base64|url|expression|data|mailto|script/i,

        rx = {
            outer: hx,
            html: hx,
            style: sx,
            styleproperty: ssx
        };


    function F() {}     // Used by Object.create

// Provide critical ES5 functions to ES3.

    if (typeof Array.prototype.filter !== 'function') {
        Array.prototype.filter = function (f) {
            var i, length = this.length, result = [], value;
            for (i = 0; i < length; i += 1) {
                try {
                    value = this[i];
                    if (f(value)) {
                        result.push(value);
                    }
                } catch (ignore) {
                }
            }
            return result;
        };
    }

    if (typeof Array.prototype.forEach !== 'function') {
        Array.prototype.forEach = function (f) {
            var i, length = this.length;
            for (i = 0; i < length; i += 1) {
                try {
                    f(this[i]);
                } catch (ignore) {
                }
            }
        };
    }

    if (typeof Array.isArray !== 'function') {
        Array.isArray = function (o) {
            return Object.prototype.toString.apply(o) === '[object Array]';
        };
    }

    if (!Object.prototype.hasOwnProperty.call(Object, 'create')) {
        Object.create = function (o) {
            F.prototype = o;
            return new F();
        };
    }

    if (typeof Object.keys !== 'function') {
        Object.keys = function (o) {
            var array = [], key;
            for (key in o) {
                if (Object.prototype.hasOwnProperty.call(o, key)) {
                    array.push(key);
                }
            }
            return array;
        };
    }

    if (typeof String.prototype.entityify !== 'function') {
        String.prototype.entityify = function () {
            return this
                .replace(/&/g, '&amp;')
                .replace(/</g, '&lt;')
                .replace(/>/g, '&gt;');
        };
    }

    if (typeof String.prototype.isAlpha !== 'function') {
        String.prototype.isAlpha = function () {
            return (this >= 'a' && this <= 'z\uffff') ||
                (this >= 'A' && this <= 'Z\uffff');
        };
    }

    if (typeof String.prototype.isDigit !== 'function') {
        String.prototype.isDigit = function () {
            return (this >= '0' && this <= '9');
        };
    }

    if (typeof String.prototype.supplant !== 'function') {
        String.prototype.supplant = function (o) {
            return this.replace(/\{([^{}]*)\}/g, function (a, b) {
                var replacement = o[b];
                return typeof replacement === 'string' ||
                    typeof replacement === 'number' ? replacement : a;
            });
        };
    }


    function sanitize(a) {

//  Escapify a troublesome character.

        return escapes[a] ||
            '\\u' + ('0000' + a.charCodeAt().toString(16)).slice(-4);
    }


    function add_to_predefined(group) {
        Object.keys(group).forEach(function (name) {
            predefined[name] = group[name];
        });
    }


    function assume() {
        if (!option.safe) {
            if (option.rhino) {
                add_to_predefined(rhino);
                option.rhino = false;
            }
            if (option.devel) {
                add_to_predefined(devel);
                option.devel = false;
            }
            if (option.browser) {
                add_to_predefined(browser);
                option.browser = false;
            }
            if (option.windows) {
                add_to_predefined(windows);
                option.windows = false;
            }
            if (option.node) {
                add_to_predefined(node);
                option.node = false;
                node_js = true;
            }
            if (option.widget) {
                add_to_predefined(widget);
                option.widget = false;
            }
        }
        if (option.type) {
            option.confusion = true;
        }
    }


// Produce an error warning.

    function artifact(tok) {
        if (!tok) {
            tok = next_token;
        }
        return tok.number || tok.string;
    }

    function quit(message, line, character) {
        throw {
            name: 'JSLintError',
            line: line,
            character: character,
            message: bundle.scanned_a_b.supplant({
                a: message,
                b: Math.floor((line / lines.length) * 100)
            })
        };
    }

    function warn(message, offender, a, b, c, d) {
        var character, line, warning;
        offender = offender || next_token;  // `~
        line = offender.line || 0;
        character = offender.from || 0;
        warning = {
            id: '(error)',
            raw: bundle[message] || message,
            evidence: lines[line - 1] || '',
            line: line,
            character: character,
            a: a || (offender.id === '(number)'
                ? String(offender.number)
                : offender.string),
            b: b,
            c: c,
            d: d
        };
        warning.reason = warning.raw.supplant(warning);
        JSLINT.errors.push(warning);
        if (option.passfail) {
            quit(bundle.stopping, line, character);
        }
        warnings += 1;
        if (warnings >= option.maxerr) {
            quit(bundle.too_many, line, character);
        }
        return warning;
    }

    function warn_at(message, line, character, a, b, c, d) {
        return warn(message, {
            line: line,
            from: character
        }, a, b, c, d);
    }

    function stop(message, offender, a, b, c, d) {
        var warning = warn(message, offender, a, b, c, d);
        quit(bundle.stopping, warning.line, warning.character);
    }

    function stop_at(message, line, character, a, b, c, d) {
        return stop(message, {
            line: line,
            from: character
        }, a, b, c, d);
    }

    function expected_at(at) {
        if (!option.white && next_token.from !== at) {
            warn('expected_a_at_b_c', next_token, '', at,
                next_token.from);
        }
    }

    function aint(it, name, expected) {
        if (it[name] !== expected) {
            warn('expected_a_b', it, expected, it[name]);
            return true;
        } else {
            return false;
        }
    }


// lexical analysis and token construction

    lex = (function lex() {
        var character, c, from, length, line, pos, source_row;

// Private lex methods

        function next_line() {
            var at;
            if (line >= lines.length) {
                return false;
            }
            character = 1;
            source_row = lines[line];
            line += 1;
            at = source_row.search(/ \t/);
            if (at >= 0) {
                warn_at('mixed', line, at + 1);
            }
            source_row = source_row.replace(/\t/g, tab);
            at = source_row.search(cx);
            if (at >= 0) {
                warn_at('unsafe', line, at);
            }
            if (option.maxlen && option.maxlen < source_row.length) {
                warn_at('too_long', line, source_row.length);
            }
            return true;
        }

// Produce a token object.  The token inherits from a syntax symbol.

        function it(type, value) {
            var id, the_token;
            if (type === '(string)' || type === '(range)') {
                if (jx.test(value)) {
                    warn_at('url', line, from);
                }
            }
            the_token = Object.create(syntax[(
                type === '(punctuator)' || (type === '(identifier)' &&
                        Object.prototype.hasOwnProperty.call(syntax, value))
                    ? value
                    : type
            )] || syntax['(error)']);
            if (type === '(identifier)') {
                the_token.identifier = true;
                if (value === '__iterator__' || value === '__proto__') {
                    stop_at('reserved_a', line, from, value);
                } else if (!option.nomen &&
                        (value.charAt(0) === '_' ||
                        value.charAt(value.length - 1) === '_')) {
                    warn_at('dangling_a', line, from, value);
                }
            }
            if (type === '(number)') {
                the_token.number = +value;
            } else if (value !== undefined) {
                the_token.string = String(value);
            }
            the_token.line = line;
            the_token.from = from;
            the_token.thru = character;
            id = the_token.id;
            prereg = id && (
                ('(,=:[!&|?{};'.indexOf(id.charAt(id.length - 1)) >= 0) ||
                id === 'return' || id === 'case'
            );
            return the_token;
        }

        function match(x) {
            var exec = x.exec(source_row), first;
            if (exec) {
                length = exec[0].length;
                first = exec[1];
                c = first.charAt(0);
                source_row = source_row.slice(length);
                from = character + length - first.length;
                character += length;
                return first;
            }
        }

        function string(x) {
            var c, pos = 0, r = '', result;

            function hex(n) {
                var i = parseInt(source_row.substr(pos + 1, n), 16);
                pos += n;
                if (i >= 32 && i <= 126 &&
                        i !== 34 && i !== 92 && i !== 39) {
                    warn_at('unexpected_a', line, character, '\\');
                }
                character += n;
                c = String.fromCharCode(i);
            }

            if (json_mode && x !== '"') {
                warn_at('expected_a', line, character, '"');
            }

            if (xquote === x || (xmode === 'scriptstring' && !xquote)) {
                return it('(punctuator)', x);
            }

            for (;;) {
                while (pos >= source_row.length) {
                    pos = 0;
                    if (xmode !== 'html' || !next_line()) {
                        stop_at('unclosed', line, from);
                    }
                }
                c = source_row.charAt(pos);
                if (c === x) {
                    character += 1;
                    source_row = source_row.slice(pos + 1);
                    result = it('(string)', r);
                    result.quote = x;
                    return result;
                }
                if (c < ' ') {
                    if (c === '\n' || c === '\r') {
                        break;
                    }
                    warn_at('control_a', line, character + pos,
                        source_row.slice(0, pos));
                } else if (c === xquote) {
                    warn_at('bad_html', line, character + pos);
                } else if (c === '<') {
                    if (option.safe && xmode === 'html') {
                        warn_at('adsafe_a', line, character + pos, c);
                    } else if (source_row.charAt(pos + 1) === '/' && (xmode || option.safe)) {
                        warn_at('expected_a_b', line, character,
                            '<\\/', '</');
                    } else if (source_row.charAt(pos + 1) === '!' && (xmode || option.safe)) {
                        warn_at('unexpected_a', line, character, '<!');
                    }
                } else if (c === '\\') {
                    if (xmode === 'html') {
                        if (option.safe) {
                            warn_at('adsafe_a', line, character + pos, c);
                        }
                    } else if (xmode === 'styleproperty') {
                        pos += 1;
                        character += 1;
                        c = source_row.charAt(pos);
                        if (c !== x) {
                            warn_at('unexpected_a', line, character, '\\');
                        }
                    } else {
                        pos += 1;
                        character += 1;
                        c = source_row.charAt(pos);
                        switch (c) {
                        case '':
                            if (!option.es5) {
                                warn_at('es5', line, character);
                            }
                            next_line();
                            pos = -1;
                            break;
                        case xquote:
                            warn_at('bad_html', line, character + pos);
                            break;
                        case '\'':
                            if (json_mode) {
                                warn_at('unexpected_a', line, character, '\\\'');
                            }
                            break;
                        case 'u':
                            hex(4);
                            break;
                        case 'v':
                            if (json_mode) {
                                warn_at('unexpected_a', line, character, '\\v');
                            }
                            c = '\v';
                            break;
                        case 'x':
                            if (json_mode) {
                                warn_at('unexpected_a', line, character, '\\x');
                            }
                            hex(2);
                            break;
                        default:
                            c = descapes[c];
                            if (typeof c !== 'string') {
                                warn_at('unexpected_a', line, character, '\\');
                            }
                        }
                    }
                }
                r += c;
                character += 1;
                pos += 1;
            }
        }

        function number(snippet) {
            var digit;
            if (xmode !== 'style' && xmode !== 'styleproperty' &&
                    source_row.charAt(0).isAlpha()) {
                warn_at('expected_space_a_b',
                    line, character, c, source_row.charAt(0));
            }
            if (c === '0') {
                digit = snippet.charAt(1);
                if (digit.isDigit()) {
                    if (token.id !== '.' && xmode !== 'styleproperty') {
                        warn_at('unexpected_a', line, character, snippet);
                    }
                } else if (json_mode && (digit === 'x' || digit === 'X')) {
                    warn_at('unexpected_a', line, character, '0x');
                }
            }
            if (snippet.slice(snippet.length - 1) === '.') {
                warn_at('trailing_decimal_a', line, character, snippet);
            }
            if (xmode !== 'style') {
                digit = +snippet;
                if (!isFinite(digit)) {
                    warn_at('bad_number', line, character, snippet);
                }
                snippet = digit;
            }
            return it('(number)', snippet);
        }

        function comment(snippet) {
            if (comments_off || src || (xmode && xmode !== 'script' &&
                    xmode !== 'style' && xmode !== 'styleproperty')) {
                warn_at('unexpected_comment', line, character);
            } else if (xmode === 'script' && /<\//i.test(source_row)) {
                warn_at('unexpected_a', line, character, '<\/');
            } else if (option.safe && ax.test(snippet)) {
                warn_at('dangerous_comment', line, character);
            }
        }

        function regexp() {
            var b,
                bit,
                captures = 0,
                depth = 0,
                flag = '',
                high,
                letter,
                length = 0,
                low,
                potential,
                quote,
                result;
            for (;;) {
                b = true;
                c = source_row.charAt(length);
                length += 1;
                switch (c) {
                case '':
                    stop_at('unclosed_regexp', line, from);
                    return;
                case '/':
                    if (depth > 0) {
                        warn_at('unescaped_a', line, from + length, '/');
                    }
                    c = source_row.slice(0, length - 1);
                    potential = Object.create(regexp_flag);
                    for (;;) {
                        letter = source_row.charAt(length);
                        if (potential[letter] !== true) {
                            break;
                        }
                        potential[letter] = false;
                        length += 1;
                        flag += letter;
                    }
                    if (source_row.charAt(length).isAlpha()) {
                        stop_at('unexpected_a', line, from, source_row.charAt(length));
                    }
                    character += length;
                    source_row = source_row.slice(length);
                    quote = source_row.charAt(0);
                    if (quote === '/' || quote === '*') {
                        stop_at('confusing_regexp', line, from);
                    }
                    result = it('(regexp)', c);
                    result.flag = flag;
                    return result;
                case '\\':
                    c = source_row.charAt(length);
                    if (c < ' ') {
                        warn_at('control_a', line, from + length, String(c));
                    } else if (c === '<') {
                        warn_at(bundle.unexpected_a, line, from + length, '\\');
                    }
                    length += 1;
                    break;
                case '(':
                    depth += 1;
                    b = false;
                    if (source_row.charAt(length) === '?') {
                        length += 1;
                        switch (source_row.charAt(length)) {
                        case ':':
                        case '=':
                        case '!':
                            length += 1;
                            break;
                        default:
                            warn_at(bundle.expected_a_b, line, from + length,
                                ':', source_row.charAt(length));
                        }
                    } else {
                        captures += 1;
                    }
                    break;
                case '|':
                    b = false;
                    break;
                case ')':
                    if (depth === 0) {
                        warn_at('unescaped_a', line, from + length, ')');
                    } else {
                        depth -= 1;
                    }
                    break;
                case ' ':
                    pos = 1;
                    while (source_row.charAt(length) === ' ') {
                        length += 1;
                        pos += 1;
                    }
                    if (pos > 1) {
                        warn_at('use_braces', line, from + length, pos);
                    }
                    break;
                case '[':
                    c = source_row.charAt(length);
                    if (c === '^') {
                        length += 1;
                        if (!option.regexp) {
                            warn_at('insecure_a', line, from + length, c);
                        } else if (source_row.charAt(length) === ']') {
                            stop_at('unescaped_a', line, from + length, '^');
                        }
                    }
                    bit = false;
                    if (c === ']') {
                        warn_at('empty_class', line, from + length - 1);
                        bit = true;
                    }
klass:              do {
                        c = source_row.charAt(length);
                        length += 1;
                        switch (c) {
                        case '[':
                        case '^':
                            warn_at('unescaped_a', line, from + length, c);
                            bit = true;
                            break;
                        case '-':
                            if (bit) {
                                bit = false;
                            } else {
                                warn_at('unescaped_a', line, from + length, '-');
                                bit = true;
                            }
                            break;
                        case ']':
                            if (!bit) {
                                warn_at('unescaped_a', line, from + length - 1, '-');
                            }
                            break klass;
                        case '\\':
                            c = source_row.charAt(length);
                            if (c < ' ') {
                                warn_at(bundle.control_a, line, from + length, String(c));
                            } else if (c === '<') {
                                warn_at(bundle.unexpected_a, line, from + length, '\\');
                            }
                            length += 1;
                            bit = true;
                            break;
                        case '/':
                            warn_at('unescaped_a', line, from + length - 1, '/');
                            bit = true;
                            break;
                        case '<':
                            if (xmode === 'script') {
                                c = source_row.charAt(length);
                                if (c === '!' || c === '/') {
                                    warn_at(bundle.html_confusion_a, line,
                                        from + length, c);
                                }
                            }
                            bit = true;
                            break;
                        default:
                            bit = true;
                        }
                    } while (c);
                    break;
                case '.':
                    if (!option.regexp) {
                        warn_at('insecure_a', line, from + length, c);
                    }
                    break;
                case ']':
                case '?':
                case '{':
                case '}':
                case '+':
                case '*':
                    warn_at('unescaped_a', line, from + length, c);
                    break;
                case '<':
                    if (xmode === 'script') {
                        c = source_row.charAt(length);
                        if (c === '!' || c === '/') {
                            warn_at(bundle.html_confusion_a, line, from + length, c);
                        }
                    }
                    break;
                }
                if (b) {
                    switch (source_row.charAt(length)) {
                    case '?':
                    case '+':
                    case '*':
                        length += 1;
                        if (source_row.charAt(length) === '?') {
                            length += 1;
                        }
                        break;
                    case '{':
                        length += 1;
                        c = source_row.charAt(length);
                        if (c < '0' || c > '9') {
                            warn_at(bundle.expected_number_a, line,
                                from + length, c);
                        }
                        length += 1;
                        low = +c;
                        for (;;) {
                            c = source_row.charAt(length);
                            if (c < '0' || c > '9') {
                                break;
                            }
                            length += 1;
                            low = +c + (low * 10);
                        }
                        high = low;
                        if (c === ',') {
                            length += 1;
                            high = Infinity;
                            c = source_row.charAt(length);
                            if (c >= '0' && c <= '9') {
                                length += 1;
                                high = +c;
                                for (;;) {
                                    c = source_row.charAt(length);
                                    if (c < '0' || c > '9') {
                                        break;
                                    }
                                    length += 1;
                                    high = +c + (high * 10);
                                }
                            }
                        }
                        if (source_row.charAt(length) !== '}') {
                            warn_at(bundle.expected_a_b, line, from + length,
                                '}', c);
                        } else {
                            length += 1;
                        }
                        if (source_row.charAt(length) === '?') {
                            length += 1;
                        }
                        if (low > high) {
                            warn_at(bundle.not_greater, line, from + length,
                                low, high);
                        }
                        break;
                    }
                }
            }
            c = source_row.slice(0, length - 1);
            character += length;
            source_row = source_row.slice(length);
            return it('(regexp)', c);
        }

// Public lex methods

        return {
            init: function (source) {
                if (typeof source === 'string') {
                    lines = source
                        .replace(crlfx, '\n')
                        .replace(crx, '\n')
                        .split('\n');
                } else {
                    lines = source;
                }
                line = 0;
                next_line();
                from = 1;
            },

            range: function (begin, end) {
                var c, value = '';
                from = character;
                if (source_row.charAt(0) !== begin) {
                    stop_at('expected_a_b', line, character, begin,
                        source_row.charAt(0));
                }
                for (;;) {
                    source_row = source_row.slice(1);
                    character += 1;
                    c = source_row.charAt(0);
                    switch (c) {
                    case '':
                        stop_at('missing_a', line, character, c);
                        break;
                    case end:
                        source_row = source_row.slice(1);
                        character += 1;
                        return it('(range)', value);
                    case xquote:
                    case '\\':
                        warn_at('unexpected_a', line, character, c);
                        break;
                    }
                    value += c;
                }
            },

// token -- this is called by advance to get the next token.

            token: function () {
                var c, i, snippet;

                for (;;) {
                    while (!source_row) {
                        if (!next_line()) {
                            return it('(end)');
                        }
                    }
                    while (xmode === 'outer') {
                        i = source_row.search(ox);
                        if (i === 0) {
                            break;
                        } else if (i > 0) {
                            character += 1;
                            source_row = source_row.slice(i);
                            break;
                        } else {
                            if (!next_line()) {
                                return it('(end)', '');
                            }
                        }
                    }
                    snippet = match(rx[xmode] || tx);
                    if (!snippet) {
                        if (source_row) {
                            if (source_row.charAt(0) === ' ') {
                                if (!option.white) {
                                    warn_at('unexpected_a', line, character,
                                        '(space)');
                                }
                                character += 1;
                                source_row = '';
                            } else {
                                stop_at('unexpected_a', line, character,
                                    source_row.charAt(0));
                            }
                        }
                    } else {

//      identifier

                        c = snippet.charAt(0);
                        if (c.isAlpha() || c === '_' || c === '$') {
                            return it('(identifier)', snippet);
                        }

//      number

                        if (c.isDigit()) {
                            return number(snippet);
                        }
                        switch (snippet) {

//      string

                        case '"':
                        case "'":
                            return string(snippet);

//      // comment

                        case '//':
                            comment(source_row);
                            source_row = '';
                            break;

//      /* comment

                        case '/*':
                            for (;;) {
                                i = source_row.search(lx);
                                if (i >= 0) {
                                    break;
                                }
                                comment(source_row);
                                if (!next_line()) {
                                    stop_at('unclosed_comment', line, character);
                                }
                            }
                            comment(source_row.slice(0, i));
                            character += i + 2;
                            if (source_row.charAt(i) === '/') {
                                stop_at('nested_comment', line, character);
                            }
                            source_row = source_row.slice(i + 2);
                            break;

                        case '':
                            break;
//      /
                        case '/':
                            if (token.id === '/=') {
                                stop_at(
                                    bundle.slash_equal,
                                    line,
                                    from
                                );
                            }
                            return prereg
                                ? regexp()
                                : it('(punctuator)', snippet);

//      punctuator

                        case '<!--':
                            length = line;
//                            c = character;
                            for (;;) {
                                i = source_row.indexOf('--');
                                if (i >= 0) {
                                    break;
                                }
                                i = source_row.indexOf('<!');
                                if (i >= 0) {
                                    stop_at('nested_comment',
                                        line, character + i);
                                }
                                if (!next_line()) {
                                    stop_at('unclosed_comment', length, c);
                                }
                            }
                            length = source_row.indexOf('<!');
                            if (length >= 0 && length < i) {
                                stop_at('nested_comment',
                                    line, character + length);
                            }
                            character += i;
                            if (source_row.charAt(i + 2) !== '>') {
                                stop_at('expected_a', line, character, '-->');
                            }
                            character += 3;
                            source_row = source_row.slice(i + 3);
                            break;
                        case '#':
                            if (xmode === 'html' || xmode === 'styleproperty') {
                                for (;;) {
                                    c = source_row.charAt(0);
                                    if ((c < '0' || c > '9') &&
                                            (c < 'a' || c > 'f') &&
                                            (c < 'A' || c > 'F')) {
                                        break;
                                    }
                                    character += 1;
                                    source_row = source_row.slice(1);
                                    snippet += c;
                                }
                                if (snippet.length !== 4 && snippet.length !== 7) {
                                    warn_at('bad_color_a', line,
                                        from + length, snippet);
                                }
                                return it('(color)', snippet);
                            }
                            return it('(punctuator)', snippet);

                        default:
                            if (xmode === 'outer' && c === '&') {
                                character += 1;
                                source_row = source_row.slice(1);
                                for (;;) {
                                    c = source_row.charAt(0);
                                    character += 1;
                                    source_row = source_row.slice(1);
                                    if (c === ';') {
                                        break;
                                    }
                                    if (!((c >= '0' && c <= '9') ||
                                            (c >= 'a' && c <= 'z') ||
                                            c === '#')) {
                                        stop_at('bad_entity', line, from + length,
                                            character);
                                    }
                                }
                                break;
                            }
                            return it('(punctuator)', snippet);
                        }
                    }
                }
            }
        };
    }());


    function add_label(token, kind, name) {

// Define the symbol in the current function in the current scope.

        name = name || token.string;

// Global variables cannot be created in the safe subset. If a global variable
// already exists, do nothing. If it is predefined, define it.

        if (funct === global_funct) {
            if (option.safe) {
                warn('adsafe_a', token, name);
            }
            if (typeof global_funct[name] !== 'string') {
                token.writeable = typeof predefined[name] === 'boolean'
                    ? predefined[name]
                    : true;
                token.funct = funct;
                global_scope[name] = token;
            }
            if (kind === 'becoming') {
                kind = 'var';
            }

// Ordinary variables.

        } else {

// Warn if the variable already exists.

            if (typeof funct[name] === 'string') {
                if (funct[name] === 'undef') {
                    if (!option.undef) {
                        warn('used_before_a', token, name);
                    }
                    kind = 'var';
                } else {
                    warn('already_defined', token, name);
                }
            } else {

// Add the symbol to the current function.

                token.funct = funct;
                token.writeable = true;
                scope[name] = token;
            }
        }
        funct[name] = kind;
    }


    function peek(distance) {

// Peek ahead to a future token. The distance is how far ahead to look. The
// default is the next token.

        var found, slot = 0;

        distance = distance || 0;
        while (slot <= distance) {
            found = lookahead[slot];
            if (!found) {
                found = lookahead[slot] = lex.token();
            }
            slot += 1;
        }
        return found;
    }


    function advance(id, match) {

// Produce the next token, also looking for programming errors.

        if (indent) {

// If indentation checking was requested, then inspect all of the line breakings.
// The var statement is tricky because the names might be aligned or not. We
// look at the first line break after the var to determine the programmer's
// intention.

            if (var_mode && next_token.line !== token.line) {
                if ((var_mode !== indent || !next_token.edge) &&
                        next_token.from === indent.at -
                        (next_token.edge ? option.indent : 0)) {
                    var dent = indent;
                    for (;;) {
                        dent.at -= option.indent;
                        if (dent === var_mode) {
                            break;
                        }
                        dent = dent.was;
                    }
                    dent.open = false;
                }
                var_mode = null;
            }
            if (next_token.id === '?' && indent.mode === ':' &&
                    token.line !== next_token.line) {
                indent.at -= option.indent;
            }
            if (indent.open) {

// If the token is an edge.

                if (next_token.edge) {
                    if (next_token.edge === 'label') {
                        expected_at(1);
                    } else if (next_token.edge === 'case') {
                        expected_at(indent.at - option.indent);
                    } else if (indent.mode !== 'array' || next_token.line !== token.line) {
                        expected_at(indent.at);
                    }

// If the token is not an edge, but is the first token on the line.

                } else if (next_token.line !== token.line) {
                    if (next_token.from < indent.at + (indent.mode ===
                            'expression' ? 0 : option.indent)) {
                        expected_at(indent.at + option.indent);
                    }
                    indent.wrap = true;
                }
            } else if (next_token.line !== token.line) {
                if (next_token.edge) {
                    expected_at(indent.at);
                } else {
                    indent.wrap = true;
                    if (indent.mode === 'statement' || indent.mode === 'var') {
                        expected_at(indent.at + option.indent);
                    } else if (next_token.from < indent.at + (indent.mode ===
                            'expression' ? 0 : option.indent)) {
                        expected_at(indent.at + option.indent);
                    }
                }
            }
        }

        switch (token.id) {
        case '(number)':
            if (next_token.id === '.') {
                warn('trailing_decimal_a');
            }
            break;
        case '-':
            if (next_token.id === '-' || next_token.id === '--') {
                warn('confusing_a');
            }
            break;
        case '+':
            if (next_token.id === '+' || next_token.id === '++') {
                warn('confusing_a');
            }
            break;
        }
        if (token.id === '(string)' || token.identifier) {
            anonname = token.string;
        }

        if (id && next_token.id !== id) {
            if (match) {
                warn('expected_a_b_from_c_d', next_token, id,
                    match.id, match.line, artifact());
            } else if (!next_token.identifier || next_token.string !== id) {
                warn('expected_a_b', next_token, id, artifact());
            }
        }
        prev_token = token;
        token = next_token;
        next_token = lookahead.shift() || lex.token();
    }


    function advance_identifier(string) {
        if (next_token.identifier && next_token.string === string) {
            advance();
        } else {
            warn('expected_a_b', next_token, string, artifact());
        }
    }


    function do_safe() {
        if (option.adsafe) {
            option.safe = true;
        }
        if (option.safe) {
            option.browser     =
                option['continue'] =
                option.css     =
                option.debug   =
                option.devel   =
                option.evil    =
                option.forin   =
                option.newcap  =
                option.nomen   =
                option.on      =
                option.rhino   =
                option.sloppy  =
                option.sub     =
                option.undef   =
                option.widget  =
                option.windows = false;


            delete predefined.Array;
            delete predefined.Date;
            delete predefined.Function;
            delete predefined.Object;
            delete predefined['eval'];

            add_to_predefined({
                ADSAFE: false,
                lib: false
            });
        }
    }


    function do_globals() {
        var name, writeable;
        for (;;) {
            if (next_token.id !== '(string)' && !next_token.identifier) {
                return;
            }
            name = next_token.string;
            advance();
            writeable = false;
            if (next_token.id === ':') {
                advance(':');
                switch (next_token.id) {
                case 'true':
                    writeable = predefined[name] !== false;
                    advance('true');
                    break;
                case 'false':
                    advance('false');
                    break;
                default:
                    stop('unexpected_a');
                }
            }
            predefined[name] = writeable;
            if (next_token.id !== ',') {
                return;
            }
            advance(',');
        }
    }


    function do_jslint() {
        var name, value;
        while (next_token.id === '(string)' || next_token.identifier) {
            name = next_token.string;
            if (!allowed_option[name]) {
                stop('unexpected_a');
            }
            advance();
            if (next_token.id !== ':') {
                stop('expected_a_b', next_token, ':', artifact());
            }
            advance(':');
            if (typeof allowed_option[name] === 'number') {
                value = next_token.number;
                if (value > allowed_option[name] || value <= 0 ||
                        Math.floor(value) !== value) {
                    stop('expected_small_a');
                }
                option[name] = value;
            } else {
                if (next_token.id === 'true') {
                    option[name] = true;
                } else if (next_token.id === 'false') {
                    option[name] = false;
                } else {
                    stop('unexpected_a');
                }
            }
            advance();
            if (next_token.id === ',') {
                advance(',');
            }
        }
        assume();
    }


    function do_properties() {
        var name, type;
        option.properties = true;
        if (!funct['(old_property_type)']) {
            funct['(old_property_type)'] = property_type;
            property_type = Object.create(property_type);
        }
        for (;;) {
            if (next_token.id !== '(string)' && !next_token.identifier) {
                return;
            }
            name = next_token.string;
            type = '';
            advance();
            if (next_token.id === ':') {
                advance(':');
                if (next_token.id === 'function') {
                    advance('function');
                    if (is_type[next_token.string] === true) {
                        type = 'function ' + next_token.string;
                        advance();
                    } else {
                        type = 'function';
                    }
                } else {
                    type = next_token.string;
                    if (is_type[type] !== true) {
                        warn('expected_type_a', next_token);
                        type = '';
                    }
                    advance();
                }
            }
            property_type[name] = type;
            if (next_token.id !== ',') {
                return;
            }
            advance(',');
        }
    }


    directive = function directive() {
        var command = this.id,
            old_comments_off = comments_off,
            old_indent = indent;
        comments_off = true;
        indent = null;
        if (next_token.line === token.line && next_token.from === token.thru) {
            warn('missing_space_a_b', next_token, artifact(token), artifact());
        }
        if (lookahead.length > 0) {
            warn('unexpected_a', this);
        }
        switch (command) {
        case '/*properties':
        case '/*property':
        case '/*members':
        case '/*member':
            do_properties();
            break;
        case '/*jslint':
            if (option.safe) {
                warn('adsafe_a', this);
            }
            do_jslint();
            break;
        case '/*globals':
        case '/*global':
            if (option.safe) {
                warn('adsafe_a', this);
            }
            do_globals();
            break;
        default:
            stop('unexpected_a', this);
        }
        comments_off = old_comments_off;
        advance('*/');
        indent = old_indent;
    };


// Indentation intention

    function edge(mode) {
        next_token.edge = indent ? indent.open && (mode || 'edge') : '';
    }


    function step_in(mode) {
        var open;
        if (typeof mode === 'number') {
            indent = {
                at: +mode,
                open: true,
                was: indent
            };
        } else if (!indent) {
            indent = {
                at: 1,
                mode: 'statement',
                open: true
            };
        } else {
            open = mode === 'var' ||
                (next_token.line !== token.line && mode !== 'statement');
            indent = {
                at: (open || mode === 'control'
                    ? indent.at + option.indent
                    : indent.at) + (indent.wrap ? option.indent : 0),
                mode: mode,
                open: open,
                was: indent
            };
            if (mode === 'var' && open) {
                var_mode = indent;
            }
        }
    }

    function step_out(id, symbol) {
        if (id) {
            if (indent && indent.open) {
                indent.at -= option.indent;
                edge();
            }
            advance(id, symbol);
        }
        if (indent) {
            indent = indent.was;
        }
    }

// Functions for conformance of whitespace.

    function one_space(left, right) {
        left = left || token;
        right = right || next_token;
        if (right.id !== '(end)' && !option.white &&
                (token.line !== right.line ||
                token.thru + 1 !== right.from)) {
            warn('expected_space_a_b', right, artifact(token), artifact(right));
        }
    }

    function one_space_only(left, right) {
        left = left || token;
        right = right || next_token;
        if (right.id !== '(end)' && (left.line !== right.line ||
                (!option.white && left.thru + 1 !== right.from))) {
            warn('expected_space_a_b', right, artifact(left), artifact(right));
        }
    }

    function no_space(left, right) {
        left = left || token;
        right = right || next_token;
        if ((!option.white || xmode === 'styleproperty' || xmode === 'style') &&
                left.thru !== right.from && left.line === right.line) {
            warn('unexpected_space_a_b', right, artifact(left), artifact(right));
        }
    }

    function no_space_only(left, right) {
        left = left || token;
        right = right || next_token;
        if (right.id !== '(end)' && (left.line !== right.line ||
                (!option.white && left.thru !== right.from))) {
            warn('unexpected_space_a_b', right, artifact(left), artifact(right));
        }
    }

    function spaces(left, right) {
        if (!option.white) {
            left = left || token;
            right = right || next_token;
            if (left.thru === right.from && left.line === right.line) {
                warn('missing_space_a_b', right, artifact(left), artifact(right));
            }
        }
    }

    function comma() {
        if (next_token.id !== ',') {
            warn_at('expected_a_b', token.line, token.thru, ',', artifact());
        } else {
            if (!option.white) {
                no_space_only();
            }
            advance(',');
            spaces();
        }
    }


    function semicolon() {
        if (next_token.id !== ';') {
            warn_at('expected_a_b', token.line, token.thru, ';', artifact());
        } else {
            if (!option.white) {
                no_space_only();
            }
            advance(';');
            if (semicolon_coda[next_token.id] !== true) {
                spaces();
            }
        }
    }

    function use_strict() {
        if (next_token.string === 'use strict') {
            if (strict_mode) {
                warn('unnecessary_use');
            }
            edge();
            advance();
            semicolon();
            strict_mode = true;
            option.newcap = false;
            option.undef = false;
            return true;
        } else {
            return false;
        }
    }


    function are_similar(a, b) {
        if (a === b) {
            return true;
        }
        if (Array.isArray(a)) {
            if (Array.isArray(b) && a.length === b.length) {
                var i;
                for (i = 0; i < a.length; i += 1) {
                    if (!are_similar(a[i], b[i])) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }
        if (Array.isArray(b)) {
            return false;
        }
        if (a.id === '(number)' && b.id === '(number)') {
            return a.number === b.number;
        }
        if (a.arity === b.arity && a.string === b.string) {
            switch (a.arity) {
            case 'prefix':
            case 'suffix':
            case undefined:
                return a.id === b.id && are_similar(a.first, b.first);
            case 'infix':
                return are_similar(a.first, b.first) &&
                    are_similar(a.second, b.second);
            case 'ternary':
                return are_similar(a.first, b.first) &&
                    are_similar(a.second, b.second) &&
                    are_similar(a.third, b.third);
            case 'function':
            case 'regexp':
                return false;
            default:
                return true;
            }
        } else {
            if (a.id === '.' && b.id === '[' && b.arity === 'infix') {
                return a.second.string === b.second.string && b.second.id === '(string)';
            } else if (a.id === '[' && a.arity === 'infix' && b.id === '.') {
                return a.second.string === b.second.string && a.second.id === '(string)';
            }
        }
        return false;
    }


// This is the heart of JSLINT, the Pratt parser. In addition to parsing, it
// is looking for ad hoc lint patterns. We add .fud to Pratt's model, which is
// like .nud except that it is only used on the first token of a statement.
// Having .fud makes it much easier to define statement-oriented languages like
// JavaScript. I retained Pratt's nomenclature.

// .nud     Null denotation
// .fud     First null denotation
// .led     Left denotation
//  lbp     Left binding power
//  rbp     Right binding power

// They are elements of the parsing method called Top Down Operator Precedence.

    function expression(rbp, initial) {

// rbp is the right binding power.
// initial indicates that this is the first expression of a statement.

        var left;
        if (next_token.id === '(end)') {
            stop('unexpected_a', token, next_token.id);
        }
        advance();
        if (option.safe && scope[token.string] &&
                scope[token.string] === global_scope[token.string] &&
                (next_token.id !== '(' && next_token.id !== '.')) {
            warn('adsafe_a', token);
        }
        if (initial) {
            anonname = 'anonymous';
            funct['(verb)'] = token.string;
        }
        if (initial === true && token.fud) {
            left = token.fud();
        } else {
            if (token.nud) {
                left = token.nud();
            } else {
                if (next_token.id === '(number)' && token.id === '.') {
                    warn('leading_decimal_a', token, artifact());
                    advance();
                    return token;
                } else {
                    stop('expected_identifier_a', token, token.id);
                }
            }
            while (rbp < next_token.lbp) {
                advance();
                if (token.led) {
                    left = token.led(left);
                } else {
                    stop('expected_operator_a', token, token.id);
                }
            }
        }
        return left;
    }


// Functional constructors for making the symbols that will be inherited by
// tokens.

    function symbol(s, p) {
        var x = syntax[s];
        if (!x || typeof x !== 'object') {
            syntax[s] = x = {
                id: s,
                lbp: p || 0,
                string: s
            };
        }
        return x;
    }

    function postscript(x) {
        x.postscript = true;
        return x;
    }

    function ultimate(s) {
        var x = symbol(s, 0);
        x.from = 1;
        x.thru = 1;
        x.line = 0;
        x.edge = 'edge';
        s.string = s;
        return postscript(x);
    }


    function stmt(s, f) {
        var x = symbol(s);
        x.identifier = x.reserved = true;
        x.fud = f;
        return x;
    }

    function labeled_stmt(s, f) {
        var x = stmt(s, f);
        x.labeled = true;
    }

    function disrupt_stmt(s, f) {
        var x = stmt(s, f);
        x.disrupt = true;
    }


    function reserve_name(x) {
        var c = x.id.charAt(0);
        if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
            x.identifier = x.reserved = true;
        }
        return x;
    }


    function prefix(s, f, type) {
        var x = symbol(s, 150);
        reserve_name(x);
        x.nud = typeof f === 'function'
            ? f
            : function () {
                if (s === 'typeof') {
                    one_space();
                } else {
                    no_space_only();
                }
                this.first = expression(150);
                this.arity = 'prefix';
                if (this.id === '++' || this.id === '--') {
                    if (!option.plusplus) {
                        warn('unexpected_a', this);
                    } else if ((!this.first.identifier || this.first.reserved) &&
                            this.first.id !== '.' && this.first.id !== '[') {
                        warn('bad_operand', this);
                    }
                }
                this.type = type;
                return this;
            };
        return x;
    }


    function type(s, t, nud) {
        var x = symbol(s);
        x.arity = x.type = t;
        if (nud) {
            x.nud = nud;
        }
        return x;
    }


    function reserve(s, f) {
        var x = symbol(s);
        x.identifier = x.reserved = true;
        if (typeof f === 'function') {
            x.nud = f;
        }
        return x;
    }


    function constant(name, type) {
        var x = reserve(name);
        x.type = type;
        x.string = name;
        x.nud = return_this;
        return x;
    }


    function reservevar(s, v) {
        return reserve(s, function () {
            if (typeof v === 'function') {
                v(this);
            }
            return this;
        });
    }


    function infix(s, p, f, type, w) {
        var x = symbol(s, p);
        reserve_name(x);
        x.led = function (left) {
            this.arity = 'infix';
            if (!w) {
                spaces(prev_token, token);
                spaces();
            }
            if (!option.bitwise && this.bitwise) {
                warn('unexpected_a', this);
            }
            if (typeof f === 'function') {
                return f(left, this);
            } else {
                this.first = left;
                this.second = expression(p);
                return this;
            }
        };
        if (type) {
            x.type = type;
        }
        return x;
    }

    function expected_relation(node, message) {
        if (node.assign) {
            warn(message || bundle.conditional_assignment, node);
        }
        return node;
    }

    function expected_condition(node, message) {
        switch (node.id) {
        case '[':
        case '-':
            if (node.arity !== 'infix') {
                warn(message || bundle.weird_condition, node);
            }
            break;
        case 'false':
        case 'function':
        case 'Infinity':
        case 'NaN':
        case 'null':
        case 'true':
        case 'undefined':
        case 'void':
        case '(number)':
        case '(regexp)':
        case '(string)':
        case '{':
            warn(message || bundle.weird_condition, node);
            break;
        case '(':
            if (node.first.id === '.' && numbery[node.first.second.string] === true) {
                warn(message || bundle.weird_condition, node);
            }
            break;
        }
        return node;
    }

    function check_relation(node) {
        switch (node.arity) {
        case 'prefix':
            switch (node.id) {
            case '{':
            case '[':
                warn('unexpected_a', node);
                break;
            case '!':
                warn('confusing_a', node);
                break;
            }
            break;
        case 'function':
        case 'regexp':
            warn('unexpected_a', node);
            break;
        default:
            if (node.id  === 'NaN') {
                warn('isnan', node);
            }
        }
        return node;
    }


    function relation(s, eqeq) {
        return infix(s, 100, function (left, that) {
            check_relation(left);
            if (eqeq && !option.eqeq) {
                warn('expected_a_b', that, eqeq, that.id);
            }
            var right = expression(100);
            if (are_similar(left, right) ||
                    ((left.id === '(string)' || left.id === '(number)') &&
                    (right.id === '(string)' || right.id === '(number)'))) {
                warn('weird_relation', that);
            }
            that.first = left;
            that.second = check_relation(right);
            return that;
        }, 'boolean');
    }


    function assignop(s, op) {
        var x = infix(s, 20, function (left, that) {
            var l;
            that.first = left;
            if (left.identifier) {
                if (scope[left.string]) {
                    if (scope[left.string].writeable === false) {
                        warn('read_only', left);
                    }
                } else {
                    stop('read_only');
                }
            } else if (option.safe) {
                l = left;
                do {
                    if (typeof predefined[l.string] === 'boolean') {
                        warn('adsafe_a', l);
                    }
                    l = l.first;
                } while (l);
            }
            if (left === syntax['function']) {
                warn('identifier_function', token);
            }
            if (left.id === '.' || left.id === '[') {
                if (!left.first || left.first.string === 'arguments') {
                    warn('bad_assignment', that);
                }
            } else if (left.identifier) {
                if (!left.reserved && funct[left.string] === 'exception') {
                    warn('assign_exception', left);
                }
            } else {
                warn('bad_assignment', that);
            }
            that.second = expression(19);
            if (that.id === '=' && are_similar(that.first, that.second)) {
                warn('weird_assignment', that);
            }
            return that;
        });
        x.assign = true;
        if (op) {
            if (syntax[op].type) {
                x.type = syntax[op].type;
            }
            if (syntax[op].bitwise) {
                x.bitwise = true;
            }
        }
        return x;
    }


    function bitwise(s, p) {
        var x = infix(s, p, 'number');
        x.bitwise = true;
        return x;
    }


    function suffix(s) {
        var x = symbol(s, 150);
        x.led = function (left) {
            no_space_only(prev_token, token);
            if (!option.plusplus) {
                warn('unexpected_a', this);
            } else if ((!left.identifier || left.reserved) &&
                    left.id !== '.' && left.id !== '[') {
                warn('bad_operand', this);
            }
            this.first = left;
            this.arity = 'suffix';
            return this;
        };
        return x;
    }


    function optional_identifier() {
        if (next_token.identifier) {
            advance();
            if (option.safe && banned[token.string]) {
                warn('adsafe_a', token);
            } else if (token.reserved && !option.es5) {
                warn('expected_identifier_a_reserved', token);
            }
            return token.string;
        }
    }


    function identifier() {
        var i = optional_identifier();
        if (!i) {
            stop(token.id === 'function' && next_token.id === '('
                ? 'name_function'
                : 'expected_identifier_a');
        }
        return i;
    }


    function statement() {

        var label, old_scope = scope, the_statement;

// We don't like the empty statement.

        if (next_token.id === ';') {
            warn('unexpected_a');
            semicolon();
            return;
        }

// Is this a labeled statement?

        if (next_token.identifier && !next_token.reserved && peek().id === ':') {
            edge('label');
            label = next_token;
            advance();
            advance(':');
            scope = Object.create(old_scope);
            add_label(label, 'label');
            if (next_token.labeled !== true) {
                warn('label_a_b', next_token, label.string, artifact());
            } else if (jx.test(label.string + ':')) {
                warn('url', label);
            } else if (funct === global_funct) {
                stop('unexpected_a', token);
            }
            next_token.label = label;
        }

// Parse the statement.

        edge();
        step_in('statement');
        the_statement = expression(0, true);
        if (the_statement) {

// Look for the final semicolon.

            if (the_statement.arity === 'statement') {
                if (the_statement.id === 'switch' ||
                        (the_statement.block && the_statement.id !== 'do')) {
                    spaces();
                } else {
                    semicolon();
                }
            } else {

// If this is an expression statement, determine if it is acceptable.
// We do not like
//      new Blah();
// statments. If it is to be used at all, new should only be used to make
// objects, not side effects. The expression statements we do like do
// assignment or invocation or delete.

                if (the_statement.id === '(') {
                    if (the_statement.first.id === 'new') {
                        warn('bad_new');
                    }
                } else if (!the_statement.assign &&
                        the_statement.id !== 'delete' &&
                        the_statement.id !== '++' &&
                        the_statement.id !== '--') {
                    warn('assignment_function_expression', token);
                }
                semicolon();
            }
        }
        step_out();
        scope = old_scope;
        return the_statement;
    }


    function statements() {
        var array = [], disruptor, the_statement;

// A disrupt statement may not be followed by any other statement.
// If the last statement is disrupt, then the sequence is disrupt.

        while (next_token.postscript !== true) {
            if (next_token.id === ';') {
                warn('unexpected_a', next_token);
                semicolon();
            } else {
                if (next_token.string === 'use strict') {
                    if ((!node_js && xmode !== 'script') || funct !== global_funct || array.length > 0) {
                        warn('function_strict');
                    }
                    use_strict();
                }
                if (disruptor) {
                    warn('unreachable_a_b', next_token, next_token.string,
                        disruptor.string);
                    disruptor = null;
                }
                the_statement = statement();
                if (the_statement) {
                    array.push(the_statement);
                    if (the_statement.disrupt) {
                        disruptor = the_statement;
                        array.disrupt = true;
                    }
                }
            }
        }
        return array;
    }


    function block(ordinary) {

// array block is array sequence of statements wrapped in braces.
// ordinary is false for function bodies and try blocks.
// ordinary is true for if statements, while, etc.

        var array,
            curly = next_token,
            old_in_block = in_block,
            old_scope = scope,
            old_strict_mode = strict_mode;

        in_block = ordinary;
        scope = Object.create(scope);
        spaces();
        if (next_token.id === '{') {
            advance('{');
            step_in();
            if (!ordinary && !use_strict() && !old_strict_mode &&
                    !option.sloppy && funct['(context)'] === global_funct) {
                warn('missing_use_strict');
            }
            array = statements();
            strict_mode = old_strict_mode;
            step_out('}', curly);
        } else if (!ordinary) {
            stop('expected_a_b', next_token, '{', artifact());
        } else {
            warn('expected_a_b', next_token, '{', artifact());
            array = [statement()];
            array.disrupt = array[0].disrupt;
        }
        funct['(verb)'] = null;
        scope = old_scope;
        in_block = old_in_block;
        if (ordinary && array.length === 0) {
            warn('empty_block');
        }
        return array;
    }


    function tally_property(name) {
        if (option.properties && typeof property_type[name] !== 'string') {
            warn('unexpected_property_a', token, name);
        }
        if (typeof member[name] === 'number') {
            member[name] += 1;
        } else {
            member[name] = 1;
        }
    }


// ECMAScript parser

    syntax['(identifier)'] = {
        id: '(identifier)',
        lbp: 0,
        identifier: true,
        nud: function () {
            var name = this.string,
                variable = scope[name],
                site,
                writeable;

// If the variable is not in scope, then we may have an undeclared variable.
// Check the predefined list. If it was predefined, create the global
// variable.

            if (typeof variable !== 'object') {
                writeable = predefined[name];
                if (typeof writeable === 'boolean') {
                    global_scope[name] = variable = {
                        string:    name,
                        writeable: writeable,
                        funct:     global_funct
                    };
                    global_funct[name] = 'var';

// But if the variable is not in scope, and is not predefined, and if we are not
// in the global scope, then we have an undefined variable error.

                } else {
                    if (!option.undef) {
                        warn('used_before_a', token);
                    }
                    scope[name] = variable = {
                        string: name,
                        writeable: true,
                        funct: funct
                    };
                    funct[name] = 'undef';
                }

            }
            site = variable.funct;

// The name is in scope and defined in the current function.

            if (funct === site) {

//      Change 'unused' to 'var', and reject labels.

                switch (funct[name]) {
                case 'becoming':
                    warn('unexpected_a', token);
                    funct[name] = 'var';
                    break;
                case 'unused':
                    funct[name] = 'var';
                    break;
                case 'unparam':
                    funct[name] = 'parameter';
                    break;
                case 'unction':
                    funct[name] = 'function';
                    break;
                case 'label':
                    warn('a_label', token, name);
                    break;
                }

// If the name is already defined in the current
// function, but not as outer, then there is a scope error.

            } else {
                switch (funct[name]) {
                case 'closure':
                case 'function':
                case 'var':
                case 'unused':
                    warn('a_scope', token, name);
                    break;
                case 'label':
                    warn('a_label', token, name);
                    break;
                case 'outer':
                case 'global':
                    break;
                default:

// If the name is defined in an outer function, make an outer entry, and if
// it was unused, make it var.

                    switch (site[name]) {
                    case 'becoming':
                    case 'closure':
                    case 'function':
                    case 'parameter':
                    case 'unction':
                    case 'unused':
                    case 'var':
                        site[name] = 'closure';
                        funct[name] = site === global_funct
                            ? 'global'
                            : 'outer';
                        break;
                    case 'unparam':
                        site[name] = 'parameter';
                        funct[name] = 'outer';
                        break;
                    case 'undef':
                        funct[name] = 'undef';
                        break;
                    case 'label':
                        warn('a_label', token, name);
                        break;
                    }
                }
            }
            return this;
        },
        led: function () {
            stop('expected_operator_a');
        }
    };

// Build the syntax table by declaring the syntactic elements.

    type('(array)', 'array');
    type('(color)', 'color');
    type('(function)', 'function');
    type('(number)', 'number', return_this);
    type('(object)', 'object');
    type('(string)', 'string', return_this);
    type('(boolean)', 'boolean', return_this);
    type('(range)', 'range');
    type('(regexp)', 'regexp', return_this);

    ultimate('(begin)');
    ultimate('(end)');
    ultimate('(error)');
    postscript(symbol('</'));
    symbol('<!');
    symbol('<!--');
    symbol('-->');
    postscript(symbol('}'));
    symbol(')');
    symbol(']');
    postscript(symbol('"'));
    postscript(symbol('\''));
    symbol(';');
    symbol(':');
    symbol(',');
    symbol('#');
    symbol('@');
    symbol('*/');
    postscript(reserve('case'));
    reserve('catch');
    postscript(reserve('default'));
    reserve('else');
    reserve('finally');

    reservevar('arguments', function (x) {
        if (strict_mode && funct === global_funct) {
            warn('strict', x);
        } else if (option.safe) {
            warn('adsafe_a', x);
        }
    });
    reservevar('eval', function (x) {
        if (option.safe) {
            warn('adsafe_a', x);
        }
    });
    constant('false', 'boolean');
    constant('Infinity', 'number');
    constant('NaN', 'number');
    constant('null', '');
    reservevar('this', function (x) {
        if (option.safe) {
            warn('adsafe_a', x);
        } else if (strict_mode && funct['(token)'].arity === 'statement' &&
                funct['(name)'].charAt(0) > 'Z') {
            warn('strict', x);
        }
    });
    constant('true', 'boolean');
    constant('undefined', '');

    infix('?', 30, function (left, that) {
        step_in('?');
        that.first = expected_condition(expected_relation(left));
        that.second = expression(0);
        spaces();
        step_out();
        var colon = next_token;
        advance(':');
        step_in(':');
        spaces();
        that.third = expression(10);
        that.arity = 'ternary';
        if (are_similar(that.second, that.third)) {
            warn('weird_ternary', colon);
        } else if (are_similar(that.first, that.second)) {
            warn('use_or', that);
        }
        step_out();
        return that;
    });

    infix('||', 40, function (left, that) {
        function paren_check(that) {
            if (that.id === '&&' && !that.paren) {
                warn('and', that);
            }
            return that;
        }

        that.first = paren_check(expected_condition(expected_relation(left)));
        that.second = paren_check(expected_relation(expression(40)));
        if (are_similar(that.first, that.second)) {
            warn('weird_condition', that);
        }
        return that;
    });

    infix('&&', 50, function (left, that) {
        that.first = expected_condition(expected_relation(left));
        that.second = expected_relation(expression(50));
        if (are_similar(that.first, that.second)) {
            warn('weird_condition', that);
        }
        return that;
    });

    prefix('void', function () {
        this.first = expression(0);
        this.arity = 'prefix';
        if (option.es5) {
            warn('expected_a_b', this, 'undefined', 'void');
        } else if (this.first.number !== 0) {
            warn('expected_a_b', this.first, '0', artifact(this.first));
        }
        this.type = 'undefined';
        return this;
    });

    bitwise('|', 70);
    bitwise('^', 80);
    bitwise('&', 90);

    relation('==', '===');
    relation('===');
    relation('!=', '!==');
    relation('!==');
    relation('<');
    relation('>');
    relation('<=');
    relation('>=');

    bitwise('<<', 120);
    bitwise('>>', 120);
    bitwise('>>>', 120);

    infix('in', 120, function (left, that) {
        warn('infix_in', that);
        that.left = left;
        that.right = expression(130);
        return that;
    }, 'boolean');
    infix('instanceof', 120, null, 'boolean');
    infix('+', 130, function (left, that) {
        if (left.id === '(number)') {
            if (left.number === 0) {
                warn('unexpected_a', left, '0');
            }
        } else if (left.id === '(string)') {
            if (left.string === '') {
                warn('expected_a_b', left, 'String', '\'\'');
            }
        }
        var right = expression(130);
        if (right.id === '(number)') {
            if (right.number === 0) {
                warn('unexpected_a', right, '0');
            }
        } else if (right.id === '(string)') {
            if (right.string === '') {
                warn('expected_a_b', right, 'String', '\'\'');
            }
        }
        if (left.id === right.id) {
            if (left.id === '(string)' || left.id === '(number)') {
                if (left.id === '(string)') {
                    left.string += right.string;
                    if (jx.test(left.string)) {
                        warn('url', left);
                    }
                } else {
                    left.number += right.number;
                }
                left.thru = right.thru;
                return left;
            }
        }
        that.first = left;
        that.second = right;
        return that;
    });
    prefix('+', 'num');
    prefix('+++', function () {
        warn('confusing_a', token);
        this.first = expression(150);
        this.arity = 'prefix';
        return this;
    });
    infix('+++', 130, function (left) {
        warn('confusing_a', token);
        this.first = left;
        this.second = expression(130);
        return this;
    });
    infix('-', 130, function (left, that) {
        if ((left.id === '(number)' && left.number === 0) || left.id === '(string)') {
            warn('unexpected_a', left);
        }
        var right = expression(130);
        if ((right.id === '(number)' && right.number === 0) || right.id === '(string)') {
            warn('unexpected_a', left);
        }
        if (left.id === right.id && left.id === '(number)') {
            left.number -= right.number;
            left.thru = right.thru;
            return left;
        }
        that.first = left;
        that.second = right;
        return that;
    }, 'number');
    prefix('-');
    prefix('---', function () {
        warn('confusing_a', token);
        this.first = expression(150);
        this.arity = 'prefix';
        return this;
    });
    infix('---', 130, function (left) {
        warn('confusing_a', token);
        this.first = left;
        this.second = expression(130);
        return this;
    });
    infix('*', 140, function (left, that) {
        if ((left.id === '(number)' && (left.number === 0 || left.number === 1)) || left.id === '(string)') {
            warn('unexpected_a', left);
        }
        var right = expression(140);
        if ((right.id === '(number)' && (right.number === 0 || right.number === 1)) || right.id === '(string)') {
            warn('unexpected_a', right);
        }
        if (left.id === right.id && left.id === '(number)') {
            left.number *= right.number;
            left.thru = right.thru;
            return left;
        }
        that.first = left;
        that.second = right;
        return that;
    }, 'number');
    infix('/', 140, function (left, that) {
        if ((left.id === '(number)' && left.number === 0) || left.id === '(string)') {
            warn('unexpected_a', left);
        }
        var right = expression(140);
        if ((right.id === '(number)' && (right.number === 0 || right.number === 1)) || right.id === '(string)') {
            warn('unexpected_a', right);
        }
        if (left.id === right.id && left.id === '(number)') {
            left.number /= right.number;
            left.thru = right.thru;
            return left;
        }
        that.first = left;
        that.second = right;
        return that;
    }, 'number');
    infix('%', 140, function (left, that) {
        if ((left.id === '(number)' && (left.number === 0 || left.number === 1)) || left.id === '(string)') {
            warn('unexpected_a', left);
        }
        var right = expression(140);
        if ((right.id === '(number)' && right.number === 0) || right.id === '(string)') {
            warn('unexpected_a', right);
        }
        if (left.id === right.id && left.id === '(number)') {
            left.number %= right.number;
            left.thru = right.thru;
            return left;
        }
        that.first = left;
        that.second = right;
        return that;
    }, 'number');

    suffix('++');
    prefix('++');

    suffix('--');
    prefix('--');
    prefix('delete', function () {
        one_space();
        var p = expression(0);
        if (!p || (p.id !== '.' && p.id !== '[')) {
            warn('deleted');
        }
        this.first = p;
        return this;
    });


    prefix('~', function () {
        no_space_only();
        if (!option.bitwise) {
            warn('unexpected_a', this);
        }
        expression(150);
        return this;
    }, 'number');
    prefix('!', function () {
        no_space_only();
        this.first = expected_condition(expression(150));
        this.arity = 'prefix';
        if (bang[this.first.id] === true || this.first.assign) {
            warn('confusing_a', this);
        }
        return this;
    }, 'boolean');
    prefix('typeof', null, 'string');
    prefix('new', function () {
        one_space();
        var c = expression(160), n, p, v;
        this.first = c;
        if (c.id !== 'function') {
            if (c.identifier) {
                switch (c.string) {
                case 'Object':
                    warn('use_object', token);
                    break;
                case 'Array':
                    if (next_token.id === '(') {
                        p = next_token;
                        p.first = this;
                        advance('(');
                        if (next_token.id !== ')') {
                            n = expression(0);
                            p.second = [n];
                            if (n.type !== 'number' || next_token.id === ',') {
                                warn('use_array', p);
                            }
                            while (next_token.id === ',') {
                                advance(',');
                                p.second.push(expression(0));
                            }
                        } else {
                            warn('use_array', token);
                        }
                        advance(')', p);
                        return p;
                    }
                    warn('use_array', token);
                    break;
                case 'Number':
                case 'String':
                case 'Boolean':
                case 'Math':
                case 'JSON':
                    warn('not_a_constructor', c);
                    break;
                case 'Function':
                    if (!option.evil) {
                        warn('function_eval');
                    }
                    break;
                case 'Date':
                case 'RegExp':
                case 'this':
                    break;
                default:
                    if (c.id !== 'function') {
                        v = c.string.charAt(0);
                        if (!option.newcap && (v < 'A' || v > 'Z')) {
                            warn('constructor_name_a', token);
                        }
                    }
                }
            } else {
                if (c.id !== '.' && c.id !== '[' && c.id !== '(') {
                    warn('bad_constructor', token);
                }
            }
        } else {
            warn('weird_new', this);
        }
        if (next_token.id !== '(') {
            warn('missing_a', next_token, '()');
        }
        return this;
    });

    infix('(', 160, function (left, that) {
        var p;
        if (indent && indent.mode === 'expression') {
            no_space(prev_token, token);
        } else {
            no_space_only(prev_token, token);
        }
        if (!left.immed && left.id === 'function') {
            warn('wrap_immediate');
        }
        p = [];
        if (left.identifier) {
            if (left.string.match(/^[A-Z]([A-Z0-9_$]*[a-z][A-Za-z0-9_$]*)?$/)) {
                if (left.string !== 'Number' && left.string !== 'String' &&
                        left.string !== 'Boolean' && left.string !== 'Date') {
                    if (left.string === 'Math' || left.string === 'JSON') {
                        warn('not_a_function', left);
                    } else if (left.string === 'Object') {
                        warn('use_object', token);
                    } else if (left.string === 'Array' || !option.newcap) {
                        warn('missing_a', left, 'new');
                    }
                }
            }
        } else if (left.id === '.') {
            if (option.safe && left.first.string === 'Math' &&
                    left.second === 'random') {
                warn('adsafe_a', left);
            } else if (left.second.string === 'split' &&
                    left.first.id === '(string)') {
                warn('use_array', left.second);
            }
        }
        step_in();
        if (next_token.id !== ')') {
            no_space();
            for (;;) {
                edge();
                p.push(expression(10));
                if (next_token.id !== ',') {
                    break;
                }
                comma();
            }
        }
        no_space();
        step_out(')', that);
        if (typeof left === 'object') {
            if (left.string === 'parseInt' && p.length === 1) {
                warn('radix', left);
            }
            if (!option.evil) {
                if (left.string === 'eval' || left.string === 'Function' ||
                        left.string === 'execScript') {
                    warn('evil', left);
                } else if (p[0] && p[0].id === '(string)' &&
                        (left.string === 'setTimeout' ||
                        left.string === 'setInterval')) {
                    warn('implied_evil', left);
                }
            }
            if (!left.identifier && left.id !== '.' && left.id !== '[' &&
                    left.id !== '(' && left.id !== '&&' && left.id !== '||' &&
                    left.id !== '?') {
                warn('bad_invocation', left);
            }
        }
        that.first = left;
        that.second = p;
        return that;
    }, '', true);

    prefix('(', function () {
        step_in('expression');
        no_space();
        edge();
        if (next_token.id === 'function') {
            next_token.immed = true;
        }
        var value = expression(0);
        value.paren = true;
        no_space();
        step_out(')', this);
        if (value.id === 'function') {
            if (next_token.id === '(') {
                warn('move_invocation');
            } else {
                warn('bad_wrap', this);
            }
        }
        return value;
    });

    infix('.', 170, function (left, that) {
        no_space(prev_token, token);
        no_space();
        var name = identifier(), type;
        if (typeof name === 'string') {
            tally_property(name);
        }
        that.first = left;
        that.second = token;
        if (left && left.string === 'arguments' &&
                (name === 'callee' || name === 'caller')) {
            warn('avoid_a', left, 'arguments.' + name);
        } else if (!option.evil && left && left.string === 'document' &&
                (name === 'write' || name === 'writeln')) {
            warn('write_is_wrong', left);
        } else if (option.adsafe) {
            if (!adsafe_top && left.string === 'ADSAFE') {
                if (name === 'id' || name === 'lib') {
                    warn('adsafe_a', that);
                } else if (name === 'go') {
                    if (xmode !== 'script') {
                        warn('adsafe_a', that);
                    } else if (adsafe_went || next_token.id !== '(' ||
                            peek(0).id !== '(string)' ||
                            peek(0).string !== adsafe_id ||
                            peek(1).id !== ',') {
                        stop('adsafe_a', that, 'go');
                    }
                    adsafe_went = true;
                    adsafe_may = false;
                }
            }
            adsafe_top = false;
        }
        if (!option.evil && (name === 'eval' || name === 'execScript')) {
            warn('evil');
        } else if (option.safe) {
            for (;;) {
                if (banned[name] === true) {
                    warn('adsafe_a', token, name);
                }
                if (typeof predefined[left.string] !== 'boolean' ||    //// check for writeable
                        next_token.id === '(') {
                    break;
                }
                if (next_token.id !== '.') {
                    warn('adsafe_a', that);
                    break;
                }
                advance('.');
                token.first = that;
                token.second = name;
                that = token;
                name = identifier();
                if (typeof name === 'string') {
                    tally_property(name);
                }
            }
        }
        type = property_type[name];
        if (type && typeof type === 'string' && type !== '*') {
            that.type = type;
        }
        return that;
    }, '', true);

    infix('[', 170, function (left, that) {
        var e, s;
        no_space_only(prev_token, token);
        no_space();
        step_in();
        edge();
        e = expression(0);
        switch (e.type) {
        case 'number':
            if (e.id === '(number)' && left.id === 'arguments') {
                warn('use_param', left);
            }
            break;
        case 'string':
            if (e.id === '(string)') {
                if (option.safe && (banned[e.string] ||
                        e.string.charAt(0) === '_' || e.string.slice(-1) === '_')) {
                    warn('adsafe_subscript_a', e);
                } else if (!option.evil &&
                        (e.string === 'eval' || e.string === 'execScript')) {
                    warn('evil', e);
                } else if (!option.sub && ix.test(e.string)) {
                    s = syntax[e.string];
                    if (!s || !s.reserved) {
                        warn('subscript', e);
                    }
                }
                tally_property(e.string);
            } else if (option.safe && e.id !== 'typeof') {
                warn('adsafe_subscript_a', e);
            }
            break;
        case undefined:
            if (option.safe) {
                warn('adsafe_subscript_a', e);
            }
            break;
        default:
            if (option.safe) {
                warn('adsafe_subscript_a', e);
            }
        }
        step_out(']', that);
        no_space(prev_token, token);
        that.first = left;
        that.second = e;
        return that;
    }, '', true);

    prefix('[', function () {
        this.arity = 'prefix';
        this.first = [];
        step_in('array');
        while (next_token.id !== '(end)') {
            while (next_token.id === ',') {
                warn('unexpected_a', next_token);
                advance(',');
            }
            if (next_token.id === ']') {
                break;
            }
            indent.wrap = false;
            edge();
            this.first.push(expression(10));
            if (next_token.id === ',') {
                comma();
                if (next_token.id === ']' && !option.es5) {
                    warn('unexpected_a', token);
                    break;
                }
            } else {
                break;
            }
        }
        step_out(']', this);
        return this;
    }, 170);


    function property_name() {
        var id = optional_identifier(true);
        if (!id) {
            if (next_token.id === '(string)') {
                id = next_token.string;
                if (option.safe) {
                    if (banned[id]) {
                        warn('adsafe_a');
                    } else if (id.charAt(0) === '_' ||
                            id.charAt(id.length - 1) === '_') {
                        warn('dangling_a');
                    }
                }
                advance();
            } else if (next_token.id === '(number)') {
                id = next_token.number.toString();
                advance();
            }
        }
        return id;
    }


    function function_params() {
        var id, paren = next_token, params = [];
        advance('(');
        step_in();
        no_space();
        if (next_token.id === ')') {
            no_space();
            step_out(')', paren);
            return;
        }
        for (;;) {
            edge();
            id = identifier();
            params.push(token);
            add_label(token, option.unparam ? 'parameter' : 'unparam');
            if (next_token.id === ',') {
                comma();
            } else {
                no_space();
                step_out(')', paren);
                return params;
            }
        }
    }



    function do_function(func, name) {
        var old_funct      = funct,
            old_option     = option,
            old_scope      = scope;
        funct = {
            '(name)'     : name || '\'' + (anonname || '').replace(nx, sanitize) + '\'',
            '(line)'     : next_token.line,
            '(context)'  : old_funct,
            '(breakage)' : 0,
            '(loopage)'  : 0,
            '(scope)'    : scope,
            '(token)'    : func
        };
        option = Object.create(old_option);
        scope = Object.create(old_scope);
        functions.push(funct);
        func.name = name;
        if (name) {
            add_label(func, 'function', name);
        }
        func.writeable = false;
        func.first = funct['(params)'] = function_params();
        one_space();
        func.block = block(false);
        if (funct['(old_property_type)']) {
            property_type = funct['(old_property_type)'];
            delete funct['(old_property_type)'];
        }
        if (option.confusion) {
            funct['(confusion)'] = true;
        }
        funct      = old_funct;
        option     = old_option;
        scope      = old_scope;
    }


    assignop('=');
    assignop('+=', '+');
    assignop('-=', '-');
    assignop('*=', '*');
    assignop('/=', '/').nud = function () {
        stop('slash_equal');
    };
    assignop('%=', '%');
    assignop('&=', '&');
    assignop('|=', '|');
    assignop('^=', '^');
    assignop('<<=', '<<');
    assignop('>>=', '>>');
    assignop('>>>=', '>>>');


    prefix('{', function () {
        var get, i, j, name, p, set, seen = {};
        this.arity = 'prefix';
        this.first = [];
        step_in();
        while (next_token.id !== '}') {
            indent.wrap = false;

// JSLint recognizes the ES5 extension for get/set in object literals,
// but requires that they be used in pairs.

            edge();
            if (next_token.string === 'get' && peek().id !== ':') {
                if (!option.es5) {
                    warn('es5');
                }
                get = next_token;
                advance('get');
                one_space_only();
                name = next_token;
                i = property_name();
                if (!i) {
                    stop('missing_property');
                }
                get.string = '';
                do_function(get);
                if (funct['(loopage)']) {
                    warn('function_loop', get);
                }
                p = get.first;
                if (p) {
                    warn('parameter_a_get_b', p[0], p[0].string, i);
                }
                comma();
                set = next_token;
                spaces();
                edge();
                advance('set');
                set.string = '';
                one_space_only();
                j = property_name();
                if (i !== j) {
                    stop('expected_a_b', token, i, j || next_token.string);
                }
                do_function(set);
                if (set.block.length === 0) {
                    warn('missing_a', token, 'throw');
                }
                p = set.first;
                if (!p || p.length !== 1) {
                    stop('parameter_set_a', set, 'value');
                } else if (p[0].string !== 'value') {
                    stop('expected_a_b', p[0], 'value', p[0].string);
                }
                name.first = [get, set];
            } else {
                name = next_token;
                i = property_name();
                if (typeof i !== 'string') {
                    stop('missing_property');
                }
                advance(':');
                spaces();
                name.first = expression(10);
            }
            this.first.push(name);
            if (seen[i] === true) {
                warn('duplicate_a', next_token, i);
            }
            seen[i] = true;
            tally_property(i);
            if (next_token.id !== ',') {
                break;
            }
            for (;;) {
                comma();
                if (next_token.id !== ',') {
                    break;
                }
                warn('unexpected_a', next_token);
            }
            if (next_token.id === '}' && !option.es5) {
                warn('unexpected_a', token);
            }
        }
        step_out('}', this);
        return this;
    });

    stmt('{', function () {
        warn('statement_block');
        this.arity = 'statement';
        this.block = statements();
        this.disrupt = this.block.disrupt;
        advance('}', this);
        return this;
    });

    stmt('/*global', directive);
    stmt('/*globals', directive);
    stmt('/*jslint', directive);
    stmt('/*member', directive);
    stmt('/*members', directive);
    stmt('/*property', directive);
    stmt('/*properties', directive);

    stmt('var', function () {

// JavaScript does not have block scope. It only has function scope. So,
// declaring a variable in a block can have unexpected consequences.

// var.first will contain an array, the array containing name tokens
// and assignment tokens.

        var assign, id, name;

        if (funct['(vars)'] && !option.vars) {
            warn('combine_var');
        } else if (funct !== global_funct) {
            funct['(vars)'] = true;
        }
        this.arity = 'statement';
        this.first = [];
        step_in('var');
        for (;;) {
            name = next_token;
            id = identifier();
            add_label(name, 'becoming');

            if (next_token.id === '=') {
                assign = next_token;
                assign.first = name;
                spaces();
                advance('=');
                spaces();
                if (next_token.id === 'undefined') {
                    warn('unnecessary_initialize', token, id);
                }
                if (peek(0).id === '=' && next_token.identifier) {
                    stop('var_a_not');
                }
                assign.second = expression(0);
                assign.arity = 'infix';
                this.first.push(assign);
            } else {
                this.first.push(name);
            }
            if (funct[id] === 'becoming') {
                funct[id] = 'unused';
            }
            if (next_token.id !== ',') {
                break;
            }
            comma();
            indent.wrap = false;
            if (var_mode && next_token.line === token.line &&
                    this.first.length === 1) {
                var_mode = null;
                indent.open = false;
                indent.at -= option.indent;
            }
            spaces();
            edge();
        }
        var_mode = null;
        step_out();
        return this;
    });

    stmt('function', function () {
        one_space();
        if (in_block) {
            warn('function_block', token);
        }
        var name = next_token, id = identifier();
        add_label(name, 'unction');
        no_space();
        this.arity = 'statement';
        do_function(this, id);
        if (next_token.id === '(' && next_token.line === token.line) {
            stop('function_statement');
        }
        return this;
    });

    prefix('function', function () {
        one_space();
        var id = optional_identifier();
        if (id) {
            no_space();
        } else {
            id = '';
        }
        do_function(this, id);
        if (funct['(loopage)']) {
            warn('function_loop');
        }
        this.arity = 'function';
        return this;
    });

    stmt('if', function () {
        var paren = next_token;
        one_space();
        advance('(');
        step_in('control');
        no_space();
        edge();
        this.arity = 'statement';
        this.first = expected_condition(expected_relation(expression(0)));
        no_space();
        step_out(')', paren);
        one_space();
        this.block = block(true);
        if (next_token.id === 'else') {
            one_space();
            advance('else');
            one_space();
            this['else'] = next_token.id === 'if' || next_token.id === 'switch'
                ? statement(true)
                : block(true);
            if (this['else'].disrupt && this.block.disrupt) {
                this.disrupt = true;
            }
        }
        return this;
    });

    stmt('try', function () {

// try.first    The catch variable
// try.second   The catch clause
// try.third    The finally clause
// try.block    The try block

        var exception_variable, old_scope, paren;
        if (option.adsafe) {
            warn('adsafe_a', this);
        }
        one_space();
        this.arity = 'statement';
        this.block = block(false);
        if (next_token.id === 'catch') {
            one_space();
            advance('catch');
            one_space();
            paren = next_token;
            advance('(');
            step_in('control');
            no_space();
            edge();
            old_scope = scope;
            scope = Object.create(old_scope);
            exception_variable = next_token.string;
            this.first = exception_variable;
            if (!next_token.identifier) {
                warn('expected_identifier_a', next_token);
            } else {
                add_label(next_token, 'exception');
            }
            advance();
            no_space();
            step_out(')', paren);
            one_space();
            this.second = block(false);
            scope = old_scope;
        }
        if (next_token.id === 'finally') {
            one_space();
            advance('finally');
            one_space();
            this.third = block(false);
        } else if (!this.second) {
            stop('expected_a_b', next_token, 'catch', artifact());
        }
        return this;
    });

    labeled_stmt('while', function () {
        one_space();
        var paren = next_token;
        funct['(breakage)'] += 1;
        funct['(loopage)'] += 1;
        advance('(');
        step_in('control');
        no_space();
        edge();
        this.arity = 'statement';
        this.first = expected_relation(expression(0));
        if (this.first.id !== 'true') {
            expected_condition(this.first, bundle.unexpected_a);
        }
        no_space();
        step_out(')', paren);
        one_space();
        this.block = block(true);
        if (this.block.disrupt) {
            warn('strange_loop', prev_token);
        }
        funct['(breakage)'] -= 1;
        funct['(loopage)'] -= 1;
        return this;
    });

    reserve('with');

    labeled_stmt('switch', function () {

// switch.first         the switch expression
// switch.second        the array of cases. A case is 'case' or 'default' token:
//    case.first        the array of case expressions
//    case.second       the array of statements
// If all of the arrays of statements are disrupt, then the switch is disrupt.

        var cases = [],
            old_in_block = in_block,
            particular,
            the_case = next_token,
            unbroken = true;

        function find_duplicate_case(value) {
            if (are_similar(particular, value)) {
                warn('duplicate_a', value);
            }
        }

        funct['(breakage)'] += 1;
        one_space();
        advance('(');
        no_space();
        step_in();
        this.arity = 'statement';
        this.first = expected_condition(expected_relation(expression(0)));
        no_space();
        step_out(')', the_case);
        one_space();
        advance('{');
        step_in();
        in_block = true;
        this.second = [];
        while (next_token.id === 'case') {
            the_case = next_token;
            cases.forEach(find_duplicate_case);
            the_case.first = [];
            the_case.arity = 'case';
            spaces();
            edge('case');
            advance('case');
            for (;;) {
                one_space();
                particular = expression(0);
                cases.forEach(find_duplicate_case);
                cases.push(particular);
                the_case.first.push(particular);
                if (particular.id === 'NaN') {
                    warn('unexpected_a', particular);
                }
                no_space_only();
                advance(':');
                if (next_token.id !== 'case') {
                    break;
                }
                spaces();
                edge('case');
                advance('case');
            }
            spaces();
            the_case.second = statements();
            if (the_case.second && the_case.second.length > 0) {
                particular = the_case.second[the_case.second.length - 1];
                if (particular.disrupt) {
                    if (particular.id === 'break') {
                        unbroken = false;
                    }
                } else {
                    warn('missing_a_after_b', next_token, 'break', 'case');
                }
            } else {
                warn('empty_case');
            }
            this.second.push(the_case);
        }
        if (this.second.length === 0) {
            warn('missing_a', next_token, 'case');
        }
        if (next_token.id === 'default') {
            spaces();
            the_case = next_token;
            the_case.arity = 'case';
            edge('case');
            advance('default');
            no_space_only();
            advance(':');
            spaces();
            the_case.second = statements();
            if (the_case.second && the_case.second.length > 0) {
                particular = the_case.second[the_case.second.length - 1];
                if (unbroken && particular.disrupt && particular.id !== 'break') {
                    this.disrupt = true;
                }
            }
            this.second.push(the_case);
        }
        funct['(breakage)'] -= 1;
        spaces();
        step_out('}', this);
        in_block = old_in_block;
        return this;
    });

    stmt('debugger', function () {
        if (!option.debug) {
            warn('unexpected_a', this);
        }
        this.arity = 'statement';
        return this;
    });

    labeled_stmt('do', function () {
        funct['(breakage)'] += 1;
        funct['(loopage)'] += 1;
        one_space();
        this.arity = 'statement';
        this.block = block(true);
        if (this.block.disrupt) {
            warn('strange_loop', prev_token);
        }
        one_space();
        advance('while');
        var paren = next_token;
        one_space();
        advance('(');
        step_in();
        no_space();
        edge();
        this.first = expected_condition(expected_relation(expression(0)), bundle.unexpected_a);
        no_space();
        step_out(')', paren);
        funct['(breakage)'] -= 1;
        funct['(loopage)'] -= 1;
        return this;
    });

    labeled_stmt('for', function () {

        var blok, filter, ok = false, paren = next_token, value;
        this.arity = 'statement';
        funct['(breakage)'] += 1;
        funct['(loopage)'] += 1;
        advance('(');
        if (next_token.id === ';') {
            no_space();
            advance(';');
            no_space();
            advance(';');
            no_space();
            advance(')');
            blok = block(true);
        } else {
            step_in('control');
            spaces(this, paren);
            no_space();
            if (next_token.id === 'var') {
                stop('move_var');
            }
            edge();
            if (peek(0).id === 'in') {
                this.forin = true;
                value = next_token;
                switch (funct[value.string]) {
                case 'unused':
                    funct[value.string] = 'var';
                    break;
                case 'closure':
                case 'var':
                    break;
                default:
                    warn('bad_in_a', value);
                }
                advance();
                advance('in');
                this.first = value;
                this.second = expression(20);
                step_out(')', paren);
                blok = block(true);
                if (!option.forin) {
                    if (blok.length === 1 && typeof blok[0] === 'object' &&
                            blok[0].string === 'if' && !blok[0]['else']) {
                        filter = blok[0].first;
                        while (filter.id === '&&') {
                            filter = filter.first;
                        }
                        switch (filter.id) {
                        case '===':
                        case '!==':
                            ok = filter.first.id === '['
                                ? filter.first.first.string === this.second.string &&
                                    filter.first.second.string === this.first.string
                                : filter.first.id === 'typeof' &&
                                    filter.first.first.id === '[' &&
                                    filter.first.first.first.string === this.second.string &&
                                    filter.first.first.second.string === this.first.string;
                            break;
                        case '(':
                            ok = filter.first.id === '.' && ((
                                filter.first.first.string === this.second.string &&
                                filter.first.second.string === 'hasOwnProperty' &&
                                filter.second[0].string === this.first.string
                            ) || (
                                filter.first.first.string === 'ADSAFE' &&
                                filter.first.second.string === 'has' &&
                                filter.second[0].string === this.second.string &&
                                filter.second[1].string === this.first.string
                            ) || (
                                filter.first.first.id === '.' &&
                                filter.first.first.first.id === '.' &&
                                filter.first.first.first.first.string === 'Object' &&
                                filter.first.first.first.second.string === 'prototype' &&
                                filter.first.first.second.string === 'hasOwnProperty' &&
                                filter.first.second.string === 'call' &&
                                filter.second[0].string === this.second.string &&
                                filter.second[1].string === this.first.string
                            ));
                            break;
                        }
                    }
                    if (!ok) {
                        warn('for_if', this);
                    }
                }
            } else {
                edge();
                this.first = [];
                for (;;) {
                    this.first.push(expression(0, 'for'));
                    if (next_token.id !== ',') {
                        break;
                    }
                    comma();
                }
                semicolon();
                edge();
                this.second = expected_relation(expression(0));
                if (this.second.id !== 'true') {
                    expected_condition(this.second, bundle.unexpected_a);
                }
                semicolon(token);
                if (next_token.id === ';') {
                    stop('expected_a_b', next_token, ')', ';');
                }
                this.third = [];
                edge();
                for (;;) {
                    this.third.push(expression(0, 'for'));
                    if (next_token.id !== ',') {
                        break;
                    }
                    comma();
                }
                no_space();
                step_out(')', paren);
                one_space();
                blok = block(true);
            }
        }
        if (blok.disrupt) {
            warn('strange_loop', prev_token);
        }
        this.block = blok;
        funct['(breakage)'] -= 1;
        funct['(loopage)'] -= 1;
        return this;
    });

    disrupt_stmt('break', function () {
        var label = next_token.string;
        this.arity = 'statement';
        if (funct['(breakage)'] === 0) {
            warn('unexpected_a', this);
        }
        if (next_token.identifier && token.line === next_token.line) {
            one_space_only();
            if (funct[label] !== 'label') {
                warn('not_a_label', next_token);
            } else if (scope[label].funct !== funct) {
                warn('not_a_scope', next_token);
            }
            this.first = next_token;
            advance();
        }
        return this;
    });

    disrupt_stmt('continue', function () {
        if (!option['continue']) {
            warn('unexpected_a', this);
        }
        var label = next_token.string;
        this.arity = 'statement';
        if (funct['(breakage)'] === 0) {
            warn('unexpected_a', this);
        }
        if (next_token.identifier && token.line === next_token.line) {
            one_space_only();
            if (funct[label] !== 'label') {
                warn('not_a_label', next_token);
            } else if (scope[label].funct !== funct) {
                warn('not_a_scope', next_token);
            }
            this.first = next_token;
            advance();
        }
        return this;
    });

    disrupt_stmt('return', function () {
        if (funct === global_funct) {
            warn('unexpected_a', this);
        }
        this.arity = 'statement';
        if (next_token.id !== ';' && next_token.line === token.line) {
            one_space_only();
            if (next_token.id === '/' || next_token.id === '(regexp)') {
                warn('wrap_regexp');
            }
            this.first = expression(20);
        }
        return this;
    });

    disrupt_stmt('throw', function () {
        this.arity = 'statement';
        one_space_only();
        this.first = expression(20);
        return this;
    });


//  Superfluous reserved words

    reserve('class');
    reserve('const');
    reserve('enum');
    reserve('export');
    reserve('extends');
    reserve('import');
    reserve('super');

// Harmony reserved words

    reserve('implements');
    reserve('interface');
    reserve('let');
    reserve('package');
    reserve('private');
    reserve('protected');
    reserve('public');
    reserve('static');
    reserve('yield');


// Type inference

//     function get_type(one) {
//         var type;
//         if (typeof one === 'string') {
//             return one;
//         } else if (one.type) {
//             return one.type;
//         } else if (one.id === '.') {
//             type = property_type[one.second.string];
//             return typeof type === 'string' ? type : '';
//         } else {
//             return ((one.identifier && scope[one.string]) || one).type;
//         }
//     }


//     function match_type(one_type, two_type, one, two) {
//         if (one_type === two_type) {
//             return true;
//         } else {
//             if (!funct.confusion && !two.warn) {
//                 if (typeof one !== 'string') {
//                     if (one.id === '.') {
//                         one_type = '.' + one.second.string + ': ' + one_type;
//                     } else {
//                         one_type = one.string + ': ' + one_type;
//                     }
//                 }
//                 if (two.id === '.') {
//                     two_type = '.' + two.second.string + ': ' + one_type;
//                 } else {
//                     two_type = two.string + ': ' + one_type;
//                 }
//                 warn('type_confusion_a_b', two, one_type, two_type);
//                 two.warn = true;
//             }
//             return false;
//         }
//     }


//     function conform(one, two) {
//
// // The conform function takes a type string and a token, or two tokens.
//
//         var one_type = typeof one === 'string' ? one : one.type,
//             two_type = two.type,
//             two_thing;
//
// // If both tokens already have a type, and if they match, then we are done.
// // Once a token has a type, it is locked. Neither token will change, but if
// // they do not match, there will be a warning.
//
//         if (one_type) {
//             if (two_type) {
//                 match_type(one_type, two_type, one, two);
//             } else {
//
// // two does not have a type, so look deeper. If two is a variable or property,
// // then use its type if it has one, and make the deep type one's type if it
// // doesn't. If the type was *, or if there was a mismatch, don't change the
// // deep type.
//
//                 two_thing = two.id === '(identifier)'
//                     ? scope[two.string]
//                     : two.id === '.'
//                     ? property_type[two.second.string]
//                     : null;
//                 if (two_thing) {
//                     two_type = two_thing.type;
//                     if (two_type) {
//                         if (two_type !== '*') {
//                             if (!match_type(one_type, two_type, one, two)) {
//                                 return '';
//                             }
//                         }
//                     } else {
//                         two_thing.type = one_type;
//                     }
//                 }
//
// // In any case, we give two a type.
//
//                 two.type = one_type;
//                 type_state_change = true;
//                 return one_type;
//             }
//
// // one does not have a type, but two does, so do the old switcheroo.
//
//         } else {
//             if (two_type) {
//                 return conform(two, one);
//
// // Neither token has a type yet. So we have to look deeper to see if either
// // is a variable or property.
//
//             } else {
//                 if (one.id === '(identifier)') {
//                     one_type = scope[one.string].type;
//                     if (one_type && one_type !== '*') {
//                         one.type = one_type;
//                         return conform(one, two);
//                     }
//                 } else if (one.id === '.') {
//                     one_type = property_type[one.second.string];
//                     if (one_type && one_type !== '*') {
//                         one.type = scope[one.string].type;
//                         return conform(one, two);
//                     }
//                 }
//                 if (two.id === '(identifier)') {
//                     two_type = scope[two.string].type;
//                     if (two_type && two_type !== '*') {
//                         two.type = two_type;
//                         return conform(two, one);
//                     }
//                 } else if (two.id === '.') {
//                     two_type = property_type[two.second.string];
//                     if (two_type && two_type !== '*') {
//                         two.type = scope[two.string].type;
//                         return conform(two, one);
//                     }
//                 }
//             }
//         }
//
// // Return a falsy string if we were unable to determine the type of either token.
//
//         return '';
//     }

//     function conform_array(type, array) {
//         array.forEach(function (item) {
//             return conform(type, item);
//         }, type);
//     }


//     function infer(node) {
//         if (Array.isArray(node)) {
//             node.forEach(infer);
//         } else {
//             switch (node.arity) {
//             case 'statement':
//                 infer_statement[node.id](node);
//                 break;
//             case 'infix':
//                 infer(node.first);
//                 infer(node.second);
//                 switch (node.id) {
//                 case '(':
//                     conform('function', node.first);
//                     break;
//                 default:
//                     stop('unfinished');
//                 }
//                 break;
//             case 'number':
//             case 'string':
//             case 'boolean':
//                 break;
//             default:
//                 stop('unfinished');
//             }
//         }
//     }


//     infer_statement = {
//         'var': function (node) {
//             var i, item, list = node.first;
//             for (i = 0; i < list.length; i += 1) {
//                 item = list[i];
//                 if (item.id === '=') {
//                     infer(item.second);
//                     conform(item.first, item.second);
//                     conform(item.first, item);
//                 }
//             }
//         },
//         'for': function (node) {
//             infer(node.first);
//             infer(node.second);
//             if (node.forin) {
//                 conform('string', node.first);
//                 conform('object', node.second);
//             } else {
//                 infer(node.third);
//                 conform_array('number', node.first);
//                 conform('boolean', node.second);
//                 conform_array('number', node.third);
//             }
//             infer(node.block);
//         }
//     };


//     function infer_types(node) {
//         do {
//             funct = global_funct;
//             scope = global_scope;
//             type_state_change = false;
//             infer(node);
//         } while (type_state_change);
//     }


// Parse JSON

    function json_value() {

        function json_object() {
            var brace = next_token, object = {};
            advance('{');
            if (next_token.id !== '}') {
                while (next_token.id !== '(end)') {
                    while (next_token.id === ',') {
                        warn('unexpected_a', next_token);
                        advance(',');
                    }
                    if (next_token.id !== '(string)') {
                        warn('expected_string_a');
                    }
                    if (object[next_token.string] === true) {
                        warn('duplicate_a');
                    } else if (next_token.string === '__proto__') {
                        warn('dangling_a');
                    } else {
                        object[next_token.string] = true;
                    }
                    advance();
                    advance(':');
                    json_value();
                    if (next_token.id !== ',') {
                        break;
                    }
                    advance(',');
                    if (next_token.id === '}') {
                        warn('unexpected_a', token);
                        break;
                    }
                }
            }
            advance('}', brace);
        }

        function json_array() {
            var bracket = next_token;
            advance('[');
            if (next_token.id !== ']') {
                while (next_token.id !== '(end)') {
                    while (next_token.id === ',') {
                        warn('unexpected_a', next_token);
                        advance(',');
                    }
                    json_value();
                    if (next_token.id !== ',') {
                        break;
                    }
                    advance(',');
                    if (next_token.id === ']') {
                        warn('unexpected_a', token);
                        break;
                    }
                }
            }
            advance(']', bracket);
        }

        switch (next_token.id) {
        case '{':
            json_object();
            break;
        case '[':
            json_array();
            break;
        case 'true':
        case 'false':
        case 'null':
        case '(number)':
        case '(string)':
            advance();
            break;
        case '-':
            advance('-');
            no_space_only();
            advance('(number)');
            break;
        default:
            stop('unexpected_a');
        }
    }


// CSS parsing.

    function css_name() {
        if (next_token.identifier) {
            advance();
            return true;
        }
    }


    function css_number() {
        if (next_token.id === '-') {
            advance('-');
            no_space_only();
        }
        if (next_token.id === '(number)') {
            advance('(number)');
            return true;
        }
    }


    function css_string() {
        if (next_token.id === '(string)') {
            advance();
            return true;
        }
    }

    function css_color() {
        var i, number, paren, value;
        if (next_token.identifier) {
            value = next_token.string;
            if (value === 'rgb' || value === 'rgba') {
                advance();
                paren = next_token;
                advance('(');
                for (i = 0; i < 3; i += 1) {
                    if (i) {
                        comma();
                    }
                    number = next_token.number;
                    if (next_token.id !== '(number)' || number < 0) {
                        warn('expected_positive_a', next_token);
                        advance();
                    } else {
                        advance();
                        if (next_token.id === '%') {
                            advance('%');
                            if (number > 100) {
                                warn('expected_percent_a', token, number);
                            }
                        } else {
                            if (number > 255) {
                                warn('expected_small_a', token, number);
                            }
                        }
                    }
                }
                if (value === 'rgba') {
                    comma();
                    number = next_token.number;
                    if (next_token.id !== '(number)' || number < 0 || number > 1) {
                        warn('expected_fraction_a', next_token);
                    }
                    advance();
                    if (next_token.id === '%') {
                        warn('unexpected_a');
                        advance('%');
                    }
                }
                advance(')', paren);
                return true;
            } else if (css_colorData[next_token.string] === true) {
                advance();
                return true;
            }
        } else if (next_token.id === '(color)') {
            advance();
            return true;
        }
        return false;
    }


    function css_length() {
        if (next_token.id === '-') {
            advance('-');
            no_space_only();
        }
        if (next_token.id === '(number)') {
            advance();
            if (next_token.id !== '(string)' &&
                    css_lengthData[next_token.string] === true) {
                no_space_only();
                advance();
            } else if (+token.number !== 0) {
                warn('expected_linear_a');
            }
            return true;
        }
        return false;
    }


    function css_line_height() {
        if (next_token.id === '-') {
            advance('-');
            no_space_only();
        }
        if (next_token.id === '(number)') {
            advance();
            if (next_token.id !== '(string)' &&
                    css_lengthData[next_token.string] === true) {
                no_space_only();
                advance();
            }
            return true;
        }
        return false;
    }


    function css_width() {
        if (next_token.identifier) {
            switch (next_token.string) {
            case 'thin':
            case 'medium':
            case 'thick':
                advance();
                return true;
            }
        } else {
            return css_length();
        }
    }


    function css_margin() {
        if (next_token.identifier) {
            if (next_token.string === 'auto') {
                advance();
                return true;
            }
        } else {
            return css_length();
        }
    }

    function css_attr() {
        if (next_token.identifier && next_token.string === 'attr') {
            advance();
            advance('(');
            if (!next_token.identifier) {
                warn('expected_name_a');
            }
            advance();
            advance(')');
            return true;
        }
        return false;
    }


    function css_comma_list() {
        while (next_token.id !== ';') {
            if (!css_name() && !css_string()) {
                warn('expected_name_a');
            }
            if (next_token.id !== ',') {
                return true;
            }
            comma();
        }
    }


    function css_counter() {
        if (next_token.identifier && next_token.string === 'counter') {
            advance();
            advance('(');
            advance();
            if (next_token.id === ',') {
                comma();
                if (next_token.id !== '(string)') {
                    warn('expected_string_a');
                }
                advance();
            }
            advance(')');
            return true;
        }
        if (next_token.identifier && next_token.string === 'counters') {
            advance();
            advance('(');
            if (!next_token.identifier) {
                warn('expected_name_a');
            }
            advance();
            if (next_token.id === ',') {
                comma();
                if (next_token.id !== '(string)') {
                    warn('expected_string_a');
                }
                advance();
            }
            if (next_token.id === ',') {
                comma();
                if (next_token.id !== '(string)') {
                    warn('expected_string_a');
                }
                advance();
            }
            advance(')');
            return true;
        }
        return false;
    }


    function css_radius() {
        return css_length() && (next_token.id !== '(number)' || css_length());
    }


    function css_shape() {
        var i;
        if (next_token.identifier && next_token.string === 'rect') {
            advance();
            advance('(');
            for (i = 0; i < 4; i += 1) {
                if (!css_length()) {
                    warn('expected_number_a');
                    break;
                }
            }
            advance(')');
            return true;
        }
        return false;
    }


    function css_url() {
        var c, url;
        if (next_token.identifier && next_token.string === 'url') {
            next_token = lex.range('(', ')');
            url = next_token.string;
            c = url.charAt(0);
            if (c === '"' || c === '\'') {
                if (url.slice(-1) !== c) {
                    warn('bad_url_a');
                } else {
                    url = url.slice(1, -1);
                    if (url.indexOf(c) >= 0) {
                        warn('bad_url_a');
                    }
                }
            }
            if (!url) {
                warn('missing_url');
            }
            if (ux.test(url)) {
                stop('bad_url_a');
            }
            urls.push(url);
            advance();
            return true;
        }
        return false;
    }


    css_any = [css_url, function () {
        for (;;) {
            if (next_token.identifier) {
                switch (next_token.string.toLowerCase()) {
                case 'url':
                    css_url();
                    break;
                case 'expression':
                    warn('unexpected_a');
                    advance();
                    break;
                default:
                    advance();
                }
            } else {
                if (next_token.id === ';' || next_token.id === '!'  ||
                        next_token.id === '(end)' || next_token.id === '}') {
                    return true;
                }
                advance();
            }
        }
    }];


    function font_face() {
        advance_identifier('font-family');
        advance(':');
        if (!css_name() && !css_string()) {
            stop('expected_name_a');
        }
        semicolon();
        advance_identifier('src');
        advance(':');
        while (true) {
            if (next_token.string === 'local') {
                advance_identifier('local');
                advance('(');
                if (ux.test(next_token.string)) {
                    stop('bad_url_a');
                }

                if (!css_name() && !css_string()) {
                    stop('expected_name_a');
                }
                advance(')');
            } else if (!css_url()) {
                stop('expected_a_b', next_token, 'url', artifact());
            }
            if (next_token.id !== ',') {
                break;
            }
            comma();
        }
        semicolon();
    }


    css_border_style = [
        'none', 'dashed', 'dotted', 'double', 'groove',
        'hidden', 'inset', 'outset', 'ridge', 'solid'
    ];

    css_break = [
        'auto', 'always', 'avoid', 'left', 'right'
    ];

    css_media = {
        'all': true,
        'braille': true,
        'embossed': true,
        'handheld': true,
        'print': true,
        'projection': true,
        'screen': true,
        'speech': true,
        'tty': true,
        'tv': true
    };

    css_overflow = [
        'auto', 'hidden', 'scroll', 'visible'
    ];

    css_attribute_data = {
        background: [
            true, 'background-attachment', 'background-color',
            'background-image', 'background-position', 'background-repeat'
        ],
        'background-attachment': ['scroll', 'fixed'],
        'background-color': ['transparent', css_color],
        'background-image': ['none', css_url],
        'background-position': [
            2, [css_length, 'top', 'bottom', 'left', 'right', 'center']
        ],
        'background-repeat': [
            'repeat', 'repeat-x', 'repeat-y', 'no-repeat'
        ],
        'border': [true, 'border-color', 'border-style', 'border-width'],
        'border-bottom': [
            true, 'border-bottom-color', 'border-bottom-style',
            'border-bottom-width'
        ],
        'border-bottom-color': css_color,
        'border-bottom-left-radius': css_radius,
        'border-bottom-right-radius': css_radius,
        'border-bottom-style': css_border_style,
        'border-bottom-width': css_width,
        'border-collapse': ['collapse', 'separate'],
        'border-color': ['transparent', 4, css_color],
        'border-left': [
            true, 'border-left-color', 'border-left-style', 'border-left-width'
        ],
        'border-left-color': css_color,
        'border-left-style': css_border_style,
        'border-left-width': css_width,
        'border-radius': function () {
            function count(separator) {
                var n = 1;
                if (separator) {
                    advance(separator);
                }
                if (!css_length()) {
                    return false;
                }
                while (next_token.id === '(number)') {
                    if (!css_length()) {
                        return false;
                    }
                    n += 1;
                }
                if (n > 4) {
                    warn('bad_style');
                }
                return true;
            }

            return count() && (next_token.id !== '/' || count('/'));
        },
        'border-right': [
            true, 'border-right-color', 'border-right-style',
            'border-right-width'
        ],
        'border-right-color': css_color,
        'border-right-style': css_border_style,
        'border-right-width': css_width,
        'border-spacing': [2, css_length],
        'border-style': [4, css_border_style],
        'border-top': [
            true, 'border-top-color', 'border-top-style', 'border-top-width'
        ],
        'border-top-color': css_color,
        'border-top-left-radius': css_radius,
        'border-top-right-radius': css_radius,
        'border-top-style': css_border_style,
        'border-top-width': css_width,
        'border-width': [4, css_width],
        bottom: [css_length, 'auto'],
        'caption-side' : ['bottom', 'left', 'right', 'top'],
        clear: ['both', 'left', 'none', 'right'],
        clip: [css_shape, 'auto'],
        color: css_color,
        content: [
            'open-quote', 'close-quote', 'no-open-quote', 'no-close-quote',
            css_string, css_url, css_counter, css_attr
        ],
        'counter-increment': [
            css_name, 'none'
        ],
        'counter-reset': [
            css_name, 'none'
        ],
        cursor: [
            css_url, 'auto', 'crosshair', 'default', 'e-resize', 'help', 'move',
            'n-resize', 'ne-resize', 'nw-resize', 'pointer', 's-resize',
            'se-resize', 'sw-resize', 'w-resize', 'text', 'wait'
        ],
        direction: ['ltr', 'rtl'],
        display: [
            'block', 'compact', 'inline', 'inline-block', 'inline-table',
            'list-item', 'marker', 'none', 'run-in', 'table', 'table-caption',
            'table-cell', 'table-column', 'table-column-group',
            'table-footer-group', 'table-header-group', 'table-row',
            'table-row-group'
        ],
        'empty-cells': ['show', 'hide'],
        'float': ['left', 'none', 'right'],
        font: [
            'caption', 'icon', 'menu', 'message-box', 'small-caption',
            'status-bar', true, 'font-size', 'font-style', 'font-weight',
            'font-family'
        ],
        'font-family': css_comma_list,
        'font-size': [
            'xx-small', 'x-small', 'small', 'medium', 'large', 'x-large',
            'xx-large', 'larger', 'smaller', css_length
        ],
        'font-size-adjust': ['none', css_number],
        'font-stretch': [
            'normal', 'wider', 'narrower', 'ultra-condensed',
            'extra-condensed', 'condensed', 'semi-condensed',
            'semi-expanded', 'expanded', 'extra-expanded'
        ],
        'font-style': [
            'normal', 'italic', 'oblique'
        ],
        'font-variant': [
            'normal', 'small-caps'
        ],
        'font-weight': [
            'normal', 'bold', 'bolder', 'lighter', css_number
        ],
        height: [css_length, 'auto'],
        left: [css_length, 'auto'],
        'letter-spacing': ['normal', css_length],
        'line-height': ['normal', css_line_height],
        'list-style': [
            true, 'list-style-image', 'list-style-position', 'list-style-type'
        ],
        'list-style-image': ['none', css_url],
        'list-style-position': ['inside', 'outside'],
        'list-style-type': [
            'circle', 'disc', 'square', 'decimal', 'decimal-leading-zero',
            'lower-roman', 'upper-roman', 'lower-greek', 'lower-alpha',
            'lower-latin', 'upper-alpha', 'upper-latin', 'hebrew', 'katakana',
            'hiragana-iroha', 'katakana-oroha', 'none'
        ],
        margin: [4, css_margin],
        'margin-bottom': css_margin,
        'margin-left': css_margin,
        'margin-right': css_margin,
        'margin-top': css_margin,
        'marker-offset': [css_length, 'auto'],
        'max-height': [css_length, 'none'],
        'max-width': [css_length, 'none'],
        'min-height': css_length,
        'min-width': css_length,
        opacity: css_number,
        outline: [true, 'outline-color', 'outline-style', 'outline-width'],
        'outline-color': ['invert', css_color],
        'outline-style': [
            'dashed', 'dotted', 'double', 'groove', 'inset', 'none',
            'outset', 'ridge', 'solid'
        ],
        'outline-width': css_width,
        overflow: css_overflow,
        'overflow-x': css_overflow,
        'overflow-y': css_overflow,
        padding: [4, css_length],
        'padding-bottom': css_length,
        'padding-left': css_length,
        'padding-right': css_length,
        'padding-top': css_length,
        'page-break-after': css_break,
        'page-break-before': css_break,
        position: ['absolute', 'fixed', 'relative', 'static'],
        quotes: [8, css_string],
        right: [css_length, 'auto'],
        'table-layout': ['auto', 'fixed'],
        'text-align': ['center', 'justify', 'left', 'right'],
        'text-decoration': [
            'none', 'underline', 'overline', 'line-through', 'blink'
        ],
        'text-indent': css_length,
        'text-shadow': ['none', 4, [css_color, css_length]],
        'text-transform': ['capitalize', 'uppercase', 'lowercase', 'none'],
        top: [css_length, 'auto'],
        'unicode-bidi': ['normal', 'embed', 'bidi-override'],
        'vertical-align': [
            'baseline', 'bottom', 'sub', 'super', 'top', 'text-top', 'middle',
            'text-bottom', css_length
        ],
        visibility: ['visible', 'hidden', 'collapse'],
        'white-space': [
            'normal', 'nowrap', 'pre', 'pre-line', 'pre-wrap', 'inherit'
        ],
        width: [css_length, 'auto'],
        'word-spacing': ['normal', css_length],
        'word-wrap': ['break-word', 'normal'],
        'z-index': ['auto', css_number]
    };

    function style_attribute() {
        var v;
        while (next_token.id === '*' || next_token.id === '#' ||
                next_token.string === '_') {
            if (!option.css) {
                warn('unexpected_a');
            }
            advance();
        }
        if (next_token.id === '-') {
            if (!option.css) {
                warn('unexpected_a');
            }
            advance('-');
            if (!next_token.identifier) {
                warn('expected_nonstandard_style_attribute');
            }
            advance();
            return css_any;
        } else {
            if (!next_token.identifier) {
                warn('expected_style_attribute');
            } else {
                if (Object.prototype.hasOwnProperty.call(css_attribute_data,
                        next_token.string)) {
                    v = css_attribute_data[next_token.string];
                } else {
                    v = css_any;
                    if (!option.css) {
                        warn('unrecognized_style_attribute_a');
                    }
                }
            }
            advance();
            return v;
        }
    }


    function style_value(v) {

        /*jslint confusion: true */

        var i = 0,
            n,
            once,
            match,
            round,
            start = 0,
            vi;
        switch (typeof v) {
        case 'function':
            return v();
        case 'string':
            if (next_token.identifier && next_token.string === v) {
                advance();
                return true;
            }
            return false;
        }
        for (;;) {
            if (i >= v.length) {
                return false;
            }
            vi = v[i];
            i += 1;
            if (typeof vi === 'boolean') {
                break;
            } else if (typeof vi === 'number') {
                n = vi;
                vi = v[i];
                i += 1;
            } else {
                n = 1;
            }
            match = false;
            while (n > 0) {
                if (style_value(vi)) {
                    match = true;
                    n -= 1;
                } else {
                    break;
                }
            }
            if (match) {
                return true;
            }
        }
        start = i;
        once = [];
        for (;;) {
            round = false;
            for (i = start; i < v.length; i += 1) {
                if (!once[i]) {
                    if (style_value(css_attribute_data[v[i]])) {
                        match = true;
                        round = true;
                        once[i] = true;
                        break;
                    }
                }
            }
            if (!round) {
                return match;
            }
        }
    }

    function style_child() {
        if (next_token.id === '(number)') {
            advance();
            if (next_token.string === 'n' && next_token.identifier) {
                no_space_only();
                advance();
                if (next_token.id === '+') {
                    no_space_only();
                    advance('+');
                    no_space_only();
                    advance('(number)');
                }
            }
            return;
        } else {
            if (next_token.identifier &&
                    (next_token.string === 'odd' || next_token.string === 'even')) {
                advance();
                return;
            }
        }
        warn('unexpected_a');
    }

    function substyle() {
        var v;
        for (;;) {
            if (next_token.id === '}' || next_token.id === '(end)' ||
                    (xquote && next_token.id === xquote)) {
                return;
            }
            v = style_attribute();
            advance(':');
            if (next_token.identifier && next_token.string === 'inherit') {
                advance();
            } else {
                if (!style_value(v)) {
                    warn('unexpected_a');
                    advance();
                }
            }
            if (next_token.id === '!') {
                advance('!');
                no_space_only();
                if (next_token.identifier && next_token.string === 'important') {
                    advance();
                } else {
                    warn('expected_a_b',
                        next_token, 'important', artifact());
                }
            }
            if (next_token.id === '}' || next_token.id === xquote) {
                warn('expected_a_b', next_token, ';', artifact());
            } else {
                semicolon();
            }
        }
    }

    function style_selector() {
        if (next_token.identifier) {
            if (!Object.prototype.hasOwnProperty.call(html_tag, option.cap
                    ? next_token.string.toLowerCase()
                    : next_token.string)) {
                warn('expected_tagname_a');
            }
            advance();
        } else {
            switch (next_token.id) {
            case '>':
            case '+':
                advance();
                style_selector();
                break;
            case ':':
                advance(':');
                switch (next_token.string) {
                case 'active':
                case 'after':
                case 'before':
                case 'checked':
                case 'disabled':
                case 'empty':
                case 'enabled':
                case 'first-child':
                case 'first-letter':
                case 'first-line':
                case 'first-of-type':
                case 'focus':
                case 'hover':
                case 'last-child':
                case 'last-of-type':
                case 'link':
                case 'only-of-type':
                case 'root':
                case 'target':
                case 'visited':
                    advance_identifier(next_token.string);
                    break;
                case 'lang':
                    advance_identifier('lang');
                    advance('(');
                    if (!next_token.identifier) {
                        warn('expected_lang_a');
                    }
                    advance(')');
                    break;
                case 'nth-child':
                case 'nth-last-child':
                case 'nth-last-of-type':
                case 'nth-of-type':
                    advance_identifier(next_token.string);
                    advance('(');
                    style_child();
                    advance(')');
                    break;
                case 'not':
                    advance_identifier('not');
                    advance('(');
                    if (next_token.id === ':' && peek(0).string === 'not') {
                        warn('not');
                    }
                    style_selector();
                    advance(')');
                    break;
                default:
                    warn('expected_pseudo_a');
                }
                break;
            case '#':
                advance('#');
                if (!next_token.identifier) {
                    warn('expected_id_a');
                }
                advance();
                break;
            case '*':
                advance('*');
                break;
            case '.':
                advance('.');
                if (!next_token.identifier) {
                    warn('expected_class_a');
                }
                advance();
                break;
            case '[':
                advance('[');
                if (!next_token.identifier) {
                    warn('expected_attribute_a');
                }
                advance();
                if (next_token.id === '=' || next_token.string === '~=' ||
                        next_token.string === '$=' ||
                        next_token.string === '|=' ||
                        next_token.id === '*=' ||
                        next_token.id === '^=') {
                    advance();
                    if (next_token.id !== '(string)') {
                        warn('expected_string_a');
                    }
                    advance();
                }
                advance(']');
                break;
            default:
                stop('expected_selector_a');
            }
        }
    }

    function style_pattern() {
        if (next_token.id === '{') {
            warn('expected_style_pattern');
        }
        for (;;) {
            style_selector();
            if (next_token.id === '</' || next_token.id === '{' ||
                    next_token.id === '}' || next_token.id === '(end)') {
                return '';
            }
            if (next_token.id === ',') {
                comma();
            }
        }
    }

    function style_list() {
        while (next_token.id !== '}' && next_token.id !== '</' &&
                next_token.id !== '(end)') {
            style_pattern();
            xmode = 'styleproperty';
            if (next_token.id === ';') {
                semicolon();
            } else {
                advance('{');
                substyle();
                xmode = 'style';
                advance('}');
            }
        }
    }

    function styles() {
        var i;
        while (next_token.id === '@') {
            i = peek();
            advance('@');
            switch (next_token.string) {
            case 'import':
                advance_identifier('import');
                if (!css_url()) {
                    warn('expected_a_b',
                        next_token, 'url', artifact());
                    advance();
                }
                semicolon();
                break;
            case 'media':
                advance_identifier('media');
                for (;;) {
                    if (!next_token.identifier || css_media[next_token.string] !== true) {
                        stop('expected_media_a');
                    }
                    advance();
                    if (next_token.id !== ',') {
                        break;
                    }
                    comma();
                }
                advance('{');
                style_list();
                advance('}');
                break;
            case 'font-face':
                advance_identifier('font-face');
                advance('{');
                font_face();
                advance('}');
                break;
            default:
                stop('expected_at_a');
            }
        }
        style_list();
    }


// Parse HTML

    function do_begin(n) {
        if (n !== 'html' && !option.fragment) {
            if (n === 'div' && option.adsafe) {
                stop('adsafe_fragment');
            } else {
                stop('expected_a_b', token, 'html', n);
            }
        }
        if (option.adsafe) {
            if (n === 'html') {
                stop('adsafe_html', token);
            }
            if (option.fragment) {
                if (n !== 'div') {
                    stop('adsafe_div', token);
                }
            } else {
                stop('adsafe_fragment', token);
            }
        }
        option.browser = true;
    }

    function do_attribute(a, v) {
        var u, x;
        if (a === 'id') {
            u = typeof v === 'string' ? v.toUpperCase() : '';
            if (ids[u] === true) {
                warn('duplicate_a', next_token, v);
            }
            if (!/^[A-Za-z][A-Za-z0-9._:\-]*$/.test(v)) {
                warn('bad_id_a', next_token, v);
            } else if (option.adsafe) {
                if (adsafe_id) {
                    if (v.slice(0, adsafe_id.length) !== adsafe_id) {
                        warn('adsafe_prefix_a', next_token, adsafe_id);
                    } else if (!/^[A-Z]+_[A-Z]+$/.test(v)) {
                        warn('adsafe_bad_id');
                    }
                } else {
                    adsafe_id = v;
                    if (!/^[A-Z]+_$/.test(v)) {
                        warn('adsafe_bad_id');
                    }
                }
            }
            x = v.search(dx);
            if (x >= 0) {
                warn('unexpected_char_a_b', token, v.charAt(x), a);
            }
            ids[u] = true;
        } else if (a === 'class' || a === 'type' || a === 'name') {
            x = v.search(qx);
            if (x >= 0) {
                warn('unexpected_char_a_b', token, v.charAt(x), a);
            }
            ids[u] = true;
        } else if (a === 'href' || a === 'background' ||
                a === 'content' || a === 'data' ||
                a.indexOf('src') >= 0 || a.indexOf('url') >= 0) {
            if (option.safe && ux.test(v)) {
                stop('bad_url_a', next_token, v);
            }
            urls.push(v);
        } else if (a === 'for') {
            if (option.adsafe) {
                if (adsafe_id) {
                    if (v.slice(0, adsafe_id.length) !== adsafe_id) {
                        warn('adsafe_prefix_a', next_token, adsafe_id);
                    } else if (!/^[A-Z]+_[A-Z]+$/.test(v)) {
                        warn('adsafe_bad_id');
                    }
                } else {
                    warn('adsafe_bad_id');
                }
            }
        } else if (a === 'name') {
            if (option.adsafe && v.indexOf('_') >= 0) {
                warn('adsafe_name_a', next_token, v);
            }
        }
    }

    function do_tag(name, attribute) {
        var i, tag = html_tag[name], script, x;
        src = false;
        if (!tag) {
            stop(
                bundle.unrecognized_tag_a,
                next_token,
                name === name.toLowerCase()
                    ? name
                    : name + ' (capitalization error)'
            );
        }
        if (stack.length > 0) {
            if (name === 'html') {
                stop('unexpected_a', token, name);
            }
            x = tag.parent;
            if (x) {
                if (x.indexOf(' ' + stack[stack.length - 1].name + ' ') < 0) {
                    stop('tag_a_in_b', token, name, x);
                }
            } else if (!option.adsafe && !option.fragment) {
                i = stack.length;
                do {
                    if (i <= 0) {
                        stop('tag_a_in_b', token, name, 'body');
                    }
                    i -= 1;
                } while (stack[i].name !== 'body');
            }
        }
        switch (name) {
        case 'div':
            if (option.adsafe && stack.length === 1 && !adsafe_id) {
                warn('adsafe_missing_id');
            }
            break;
        case 'script':
            xmode = 'script';
            advance('>');
            if (attribute.lang) {
                warn('lang', token);
            }
            if (option.adsafe && stack.length !== 1) {
                warn('adsafe_placement', token);
            }
            if (attribute.src) {
                if (option.adsafe && (!adsafe_may || !approved[attribute.src])) {
                    warn('adsafe_source', token);
                }
                if (attribute.type) {
                    warn('type', token);
                }
            } else {
                step_in(next_token.from);
                edge();
                use_strict();
                adsafe_top = true;
                script = statements();

// JSLint is also the static analyzer for ADsafe. See www.ADsafe.org.

                if (option.adsafe) {
                    if (adsafe_went) {
                        stop('adsafe_script', token);
                    }
                    if (script.length !== 1 ||
                            aint(script[0],             'id',     '(') ||
                            aint(script[0].first,       'id',     '.') ||
                            aint(script[0].first.first, 'string', 'ADSAFE') ||
                            aint(script[0].second[0],   'string', adsafe_id)) {
                        stop('adsafe_id_go');
                    }
                    switch (script[0].first.second.string) {
                    case 'id':
                        if (adsafe_may || adsafe_went ||
                                script[0].second.length !== 1) {
                            stop('adsafe_id', next_token);
                        }
                        adsafe_may = true;
                        break;
                    case 'go':
                        if (adsafe_went) {
                            stop('adsafe_go');
                        }
                        if (script[0].second.length !== 2 ||
                                aint(script[0].second[1], 'id', 'function') ||
                                !script[0].second[1].first ||
                                aint(script[0].second[1].first[0], 'string', 'dom') ||
                                script[0].second[1].first.length > 2 ||
                                (script[0].second[1].first.length === 2 &&
                                aint(script[0].second[1].first[1], 'string', 'lib'))) {
                            stop('adsafe_go', next_token);
                        }
                        adsafe_went = true;
                        break;
                    default:
                        stop('adsafe_id_go');
                    }
                }
                indent = null;
            }
            xmode = 'html';
            advance('</');
            advance_identifier('script');
            xmode = 'outer';
            break;
        case 'style':
            xmode = 'style';
            advance('>');
            styles();
            xmode = 'html';
            advance('</');
            advance_identifier('style');
            break;
        case 'input':
            switch (attribute.type) {
            case 'button':
            case 'checkbox':
            case 'radio':
            case 'reset':
            case 'submit':
                break;
            case 'file':
            case 'hidden':
            case 'image':
            case 'password':
            case 'text':
                if (option.adsafe && attribute.autocomplete !== 'off') {
                    warn('adsafe_autocomplete');
                }
                break;
            default:
                warn('bad_type');
            }
            break;
        case 'applet':
        case 'body':
        case 'embed':
        case 'frame':
        case 'frameset':
        case 'head':
        case 'iframe':
        case 'noembed':
        case 'noframes':
        case 'object':
        case 'param':
            if (option.adsafe) {
                warn('adsafe_tag', next_token, name);
            }
            break;
        }
    }


    function closetag(name) {
        return '</' + name + '>';
    }

    function html() {

        /*jslint confusion: true */

        var attribute, attributes, is_empty, name, old_white = option.white,
            quote, tag_name, tag, wmode;
        xmode = 'html';
        xquote = '';
        stack = null;
        for (;;) {
            switch (next_token.string) {
            case '<':
                xmode = 'html';
                advance('<');
                attributes = {};
                tag_name = next_token;
                name = tag_name.string;
                advance_identifier(name);
                if (option.cap) {
                    name = name.toLowerCase();
                }
                tag_name.name = name;
                if (!stack) {
                    stack = [];
                    do_begin(name);
                }
                tag = html_tag[name];
                if (typeof tag !== 'object') {
                    stop('unrecognized_tag_a', tag_name, name);
                }
                is_empty = tag.empty;
                tag_name.type = name;
                for (;;) {
                    if (next_token.id === '/') {
                        advance('/');
                        if (next_token.id !== '>') {
                            warn('expected_a_b', next_token, '>', artifact());
                        }
                        break;
                    }
                    if (next_token.id && next_token.id.charAt(0) === '>') {
                        break;
                    }
                    if (!next_token.identifier) {
                        if (next_token.id === '(end)' || next_token.id === '(error)') {
                            warn('expected_a_b', next_token, '>', artifact());
                        }
                        warn('bad_name_a');
                    }
                    option.white = false;
                    spaces();
                    attribute = next_token.string;
                    option.white = old_white;
                    advance();
                    if (!option.cap && attribute !== attribute.toLowerCase()) {
                        warn('attribute_case_a', token);
                    }
                    attribute = attribute.toLowerCase();
                    xquote = '';
                    if (Object.prototype.hasOwnProperty.call(attributes, attribute)) {
                        warn('duplicate_a', token, attribute);
                    }
                    if (attribute.slice(0, 2) === 'on') {
                        if (!option.on) {
                            warn('html_handlers');
                        }
                        xmode = 'scriptstring';
                        advance('=');
                        quote = next_token.id;
                        if (quote !== '"' && quote !== '\'') {
                            stop('expected_a_b', next_token, '"', artifact());
                        }
                        xquote = quote;
                        wmode = option.white;
                        option.white = true;
                        advance(quote);
                        use_strict();
                        statements();
                        option.white = wmode;
                        if (next_token.id !== quote) {
                            stop('expected_a_b', next_token, quote, artifact());
                        }
                        xmode = 'html';
                        xquote = '';
                        advance(quote);
                        tag = false;
                    } else if (attribute === 'style') {
                        xmode = 'scriptstring';
                        advance('=');
                        quote = next_token.id;
                        if (quote !== '"' && quote !== '\'') {
                            stop('expected_a_b', next_token, '"', artifact());
                        }
                        xmode = 'styleproperty';
                        xquote = quote;
                        advance(quote);
                        substyle();
                        xmode = 'html';
                        xquote = '';
                        advance(quote);
                        tag = false;
                    } else {
                        if (next_token.id === '=') {
                            advance('=');
                            tag = next_token.string;
                            if (!next_token.identifier &&
                                    next_token.id !== '"' &&
                                    next_token.id !== '\'' &&
                                    next_token.id !== '(string)' &&
                                    next_token.id !== '(string)' &&
                                    next_token.id !== '(color)') {
                                warn('expected_attribute_value_a', token, attribute);
                            }
                            advance();
                        } else {
                            tag = true;
                        }
                    }
                    attributes[attribute] = tag;
                    do_attribute(attribute, tag);
                }
                do_tag(name, attributes);
                if (!is_empty) {
                    stack.push(tag_name);
                }
                xmode = 'outer';
                advance('>');
                break;
            case '</':
                xmode = 'html';
                advance('</');
                if (!next_token.identifier) {
                    warn('bad_name_a');
                }
                name = next_token.string;
                if (option.cap) {
                    name = name.toLowerCase();
                }
                advance();
                if (!stack) {
                    stop('unexpected_a', next_token, closetag(name));
                }
                tag_name = stack.pop();
                if (!tag_name) {
                    stop('unexpected_a', next_token, closetag(name));
                }
                if (tag_name.name !== name) {
                    stop('expected_a_b',
                        next_token, closetag(tag_name.name), closetag(name));
                }
                if (next_token.id !== '>') {
                    stop('expected_a_b', next_token, '>', artifact());
                }
                xmode = 'outer';
                advance('>');
                break;
            case '<!':
                if (option.safe) {
                    warn('adsafe_a');
                }
                xmode = 'html';
                for (;;) {
                    advance();
                    if (next_token.id === '>' || next_token.id === '(end)') {
                        break;
                    }
                    if (next_token.string.indexOf('--') >= 0) {
                        stop('unexpected_a', next_token, '--');
                    }
                    if (next_token.string.indexOf('<') >= 0) {
                        stop('unexpected_a', next_token, '<');
                    }
                    if (next_token.string.indexOf('>') >= 0) {
                        stop('unexpected_a', next_token, '>');
                    }
                }
                xmode = 'outer';
                advance('>');
                break;
            case '(end)':
                return;
            default:
                if (next_token.id === '(end)') {
                    stop('missing_a', next_token,
                        '</' + stack[stack.length - 1].string + '>');
                } else {
                    advance();
                }
            }
            if (stack && stack.length === 0 && (option.adsafe ||
                    !option.fragment || next_token.id === '(end)')) {
                break;
            }
        }
        if (next_token.id !== '(end)') {
            stop('unexpected_a');
        }
    }


// The actual JSLINT function itself.

    itself = function JSLint(the_source, the_option) {

        var i, predef, tree;
        JSLINT.errors = [];
        JSLINT.tree = '';
        begin = prev_token = token = next_token =
            Object.create(syntax['(begin)']);
        predefined = {};
        add_to_predefined(standard);
        property_type = Object.create(standard_property_type);
        if (the_option) {
            option = Object.create(the_option);
            predef = option.predef;
            if (predef) {
                if (Array.isArray(predef)) {
                    for (i = 0; i < predef.length; i += 1) {
                        predefined[predef[i]] = true;
                    }
                } else if (typeof predef === 'object') {
                    add_to_predefined(predef);
                }
            }
            do_safe();
        } else {
            option = {};
        }
        option.indent = +option.indent || 4;
        option.maxerr = +option.maxerr || 50;
        adsafe_id = '';
        adsafe_may = adsafe_top = adsafe_went = false;
        approved = {};
        if (option.approved) {
            for (i = 0; i < option.approved.length; i += 1) {
                approved[option.approved[i]] = option.approved[i];
            }
        } else {
            approved.test = 'test';
        }
        tab = '';
        for (i = 0; i < option.indent; i += 1) {
            tab += ' ';
        }
        global_scope = scope = {};
        global_funct = funct = {
            '(scope)': scope,
            '(breakage)': 0,
            '(loopage)': 0
        };
        functions = [funct];

        comments_off = false;
        ids = {};
        in_block = false;
        indent = null;
        json_mode = false;
        lookahead = [];
        member = {};
        node_js = false;
        prereg = true;
        src = false;
        stack = null;
        strict_mode = false;
        urls = [];
        var_mode = null;
        warnings = 0;
        xmode = '';
        lex.init(the_source);

        assume();

        try {
            advance();
            if (next_token.id === '(number)') {
                stop('unexpected_a');
            } else if (next_token.string.charAt(0) === '<') {
                html();
                if (option.adsafe && !adsafe_went) {
                    warn('adsafe_go', this);
                }
            } else {
                switch (next_token.id) {
                case '{':
                case '[':
                    json_mode = true;
                    json_value();
                    break;
                case '@':
                case '*':
                case '#':
                case '.':
                case ':':
                    xmode = 'style';
                    advance();
                    if (token.id !== '@' || !next_token.identifier ||
                            next_token.string !== 'charset' || token.line !== 1 ||
                            token.from !== 1) {
                        stop('css');
                    }
                    advance();
                    if (next_token.id !== '(string)' &&
                            next_token.string !== 'UTF-8') {
                        stop('css');
                    }
                    advance();
                    semicolon();
                    styles();
                    break;

                default:
                    if (option.adsafe && option.fragment) {
                        stop('expected_a_b',
                            next_token, '<div>', artifact());
                    }

// If the first token is a semicolon, ignore it. This is sometimes used when
// files are intended to be appended to files that may be sloppy. A sloppy
// file may be depending on semicolon insertion on its last line.

                    step_in(1);
                    if (next_token.id === ';' && !node_js) {
                        semicolon();
                    }
                    adsafe_top = true;
                    tree = statements();
                    begin.first = tree;
                    JSLINT.tree = begin;
                    // infer_types(tree);
                    if (option.adsafe && (tree.length !== 1 ||
                            aint(tree[0], 'id', '(') ||
                            aint(tree[0].first, 'id', '.') ||
                            aint(tree[0].first.first, 'string', 'ADSAFE') ||
                            aint(tree[0].first.second, 'string', 'lib') ||
                            tree[0].second.length !== 2 ||
                            tree[0].second[0].id !== '(string)' ||
                            aint(tree[0].second[1], 'id', 'function'))) {
                        stop('adsafe_lib');
                    }
                    if (tree.disrupt) {
                        warn('weird_program', prev_token);
                    }
                }
            }
            indent = null;
            advance('(end)');
        } catch (e) {
            if (e) {        // `~
                JSLINT.errors.push({
                    reason    : e.message,
                    line      : e.line || next_token.line,
                    character : e.character || next_token.from
                }, null);
            }
        }
        return JSLINT.errors.length === 0;
    };


// Data summary.

    itself.data = function () {
        var data = {functions: []},
            function_data,
            globals,
            i,
            j,
            kind,
            members = [],
            name,
            the_function,
            undef = [],
            unused = [];
        if (itself.errors.length) {
            data.errors = itself.errors;
        }

        if (json_mode) {
            data.json = true;
        }

        if (urls.length > 0) {
            data.urls = urls;
        }

        globals = Object.keys(global_scope).filter(function (value) {
            return value.charAt(0) !== '(' && typeof standard[value] !== 'boolean';
        });
        if (globals.length > 0) {
            data.globals = globals;
        }

        for (i = 1; i < functions.length; i += 1) {
            the_function = functions[i];
            function_data = {};
            for (j = 0; j < functionicity.length; j += 1) {
                function_data[functionicity[j]] = [];
            }
            for (name in the_function) {
                if (Object.prototype.hasOwnProperty.call(the_function, name)) {
                    if (name.charAt(0) !== '(') {
                        kind = the_function[name];
                        if (kind === 'unction' || kind === 'unparam') {
                            kind = 'unused';
                        }
                        if (Array.isArray(function_data[kind])) {
                            function_data[kind].push(name);
                            if (kind === 'unused') {
                                unused.push({
                                    name: name,
                                    line: the_function['(line)'],
                                    'function': the_function['(name)']
                                });
                            } else if (kind === 'undef') {
                                undef.push({
                                    name: name,
                                    line: the_function['(line)'],
                                    'function': the_function['(name)']
                                });
                            }
                        }
                    }
                }
            }
            for (j = 0; j < functionicity.length; j += 1) {
                if (function_data[functionicity[j]].length === 0) {
                    delete function_data[functionicity[j]];
                }
            }
            function_data.name = the_function['(name)'];
            function_data.params = the_function['(params)'];
            function_data.line = the_function['(line)'];
            data.functions.push(function_data);
        }

        if (unused.length > 0) {
            data.unused = unused;
        }
        if (undef.length > 0) {
            data['undefined'] = undef;
        }

        members = [];
        for (name in member) {
            if (typeof member[name] === 'number') {
                data.member = member;
                break;
            }
        }

        return data;
    };


    itself.report = function (errors_only) {
        var data = itself.data(), err, evidence, i, italics, j, key, keys, length,
            mem = '', name, names, output = [], snippets, the_function, type,
            warning;

        function detail(h, value) {
            var comma_needed, singularity;
            if (Array.isArray(value)) {
                output.push('<div><i>' + h + '</i> ');
                value.sort().forEach(function (item) {
                    if (item !== singularity) {
                        singularity = item;
                        output.push((comma_needed ? ', ' : '') + singularity);
                        comma_needed = true;
                    }
                });
                output.push('</div>');
            } else if (value) {
                output.push('<div><i>' + h + '</i> ' + value + '</div>');
            }
        }

        if (data.errors || data.unused || data['undefined']) {
            err = true;
            output.push('<div id=errors><i>Error:</i>');
            if (data.errors) {
                for (i = 0; i < data.errors.length; i += 1) {
                    warning = data.errors[i];
                    if (warning) {
                        evidence = warning.evidence || '';
                        output.push('<p>Problem' + (isFinite(warning.line)
                            ? ' at line ' + String(warning.line) +
                                ' character ' + String(warning.character)
                            : '') +
                            ': ' + warning.reason.entityify() +
                            '</p><p class=evidence>' +
                            (evidence && (evidence.length > 80
                                ? evidence.slice(0, 77) + '...'
                                : evidence).entityify()) + '</p>');
                    }
                }
            }

            if (data['undefined']) {
                snippets = [];
                for (i = 0; i < data['undefined'].length; i += 1) {
                    snippets[i] = '<code><u>' + data['undefined'][i].name + '</u></code>&nbsp;<i>' +
                        String(data['undefined'][i].line) + ' </i> <small>' +
                        data['undefined'][i]['function'] + '</small>';
                }
                output.push('<p><i>Undefined variable:</i> ' + snippets.join(', ') + '</p>');
            }
            if (data.unused) {
                snippets = [];
                for (i = 0; i < data.unused.length; i += 1) {
                    snippets[i] = '<code><u>' + data.unused[i].name + '</u></code>&nbsp;<i>' +
                        String(data.unused[i].line) + ' </i> <small>' +
                        data.unused[i]['function'] + '</small>';
                }
                output.push('<p><i>Unused variable:</i> ' + snippets.join(', ') + '</p>');
            }
            if (data.json) {
                output.push('<p>JSON: bad.</p>');
            }
            output.push('</div>');
        }

        if (!errors_only) {

            output.push('<br><div id=functions>');

            if (data.urls) {
                detail("URLs<br>", data.urls, '<br>');
            }

            if (xmode === 'style') {
                output.push('<p>CSS.</p>');
            } else if (data.json && !err) {
                output.push('<p>JSON: good.</p>');
            } else if (data.globals) {
                output.push('<div><i>Global</i> ' +
                    data.globals.sort().join(', ') + '</div>');
            } else {
                output.push('<div><i>No new global variables introduced.</i></div>');
            }

            for (i = 0; i < data.functions.length; i += 1) {
                the_function = data.functions[i];
                names = [];
                if (the_function.params) {
                    for (j = 0; j < the_function.params.length; j += 1) {
                        names[j] = the_function.params[j].string;
                    }
                }
                output.push('<br><div class=function><i>' +
                    String(the_function.line) + '</i> ' +
                    the_function.name.entityify() +
                    '(' + names.join(', ') + ')</div>');
                detail('<big><b>Undefined</b></big>', the_function['undefined']);
                detail('<big><b>Unused</b></big>', the_function.unused);
                detail('Closure', the_function.closure);
                detail('Variable', the_function['var']);
                detail('Exception', the_function.exception);
                detail('Outer', the_function.outer);
                detail('Global', the_function.global);
                detail('Label', the_function.label);
            }

            if (data.member) {
                keys = Object.keys(data.member);
                if (keys.length) {
                    keys = keys.sort();
                    output.push('<br><pre id=properties>/*properties<br>');
                    mem = '    ';
                    italics = 0;
                    j = 0;
                    if (option.confusion) {
                        for (i = 0; i < keys.length; i += 1) {
                            key = keys[i];
                            if (typeof standard_property_type[key] !== 'string') {
                                name = ix.test(key)
                                    ? key
                                    : '\'' + key.entityify().replace(nx, sanitize) + '\'';
                                if (data.member[key] === 1) {
                                    name = '<i>' + name + '</i>';
                                    italics += 1;
                                    j = 1;
                                }
                                if (i < keys.length - 1) {
                                    name += ', ';
                                }
                                if (mem.length + name.length - (italics * 7) > 80) {
                                    output.push(mem + '<br>');
                                    mem = '    ';
                                    italics = j;
                                }
                                mem += name;
                                j = 0;
                            }
                        }
                    } else {
                        for (i = 0; i < keys.length; i += 1) {
                            key = keys[i];
                            type = property_type[key];
                            if (typeof type !== 'string') {
                                type = '';
                            }
                            if (standard_property_type[key] !== type) {
                                name = ix.test(key)
                                    ? key
                                    : '\'' + key.entityify().replace(nx, sanitize) + '\'';
                                length += name.length + 2;
                                if (data.member[key] === 1) {
                                    name = '<i>' + name + '</i>';
                                    italics += 1;
                                    j = 1;
                                }
                                if (type) {
                                    name += ': ' + type;
                                }
                                if (i < keys.length - 1) {
                                    name += ', ';
                                }
                                if (mem.length + name.length - (italics * 7) > 80) {
                                    output.push(mem + '<br>');
                                    mem = '    ';
                                    italics = j;
                                }
                                mem += name;
                                j = 0;
                            }
                        }
                    }
                    output.push(mem + '<br>*/</pre>');
                }
                output.push('</div>');
            }
        }
        return output.join('');
    };
    itself.jslint = itself;

    itself.edition = '2011-12-21';

    return itself;
}());
if (typeof exports === "object" && exports) {
	exports.JSHINT = JSLINT;
}

})()
},
{"events":2,"../shared/vars.js":3,"../shared/messages.js":10,"./lex.js":11,"./reg.js":4,"./state.js":5,"./style.js":6,"console-browserify":7,"underscore":12}],
10:[function(req,module,exports){
(function(){

var _ = req("underscore");

var errors = {
	E001: "Bad option: '{a}'.",
	E002: "Bad option value.",
	E003: "Expected a JSON value.",
	E004: "Input is neither a string nor an array of strings.",
	E005: "Input is empty.",
	E006: "Unexpected early end of program.",
	E007: "Missing \"use strict\" statement.",
	E008: "Strict violation.",
	E009: "Option 'validthis' can't be used in a global scope.",
	E010: "'with' is not allowed in strict mode.",
	E011: "const '{a}' has already been declared.",
	E012: "const '{a}' is initialized to 'undefined'.",
	E013: "Attempting to override '{a}' which is a constant.",
	E014: "A regular expression literal can be confused with '/='.",
	E015: "Unclosed regular expression.",
	E016: "Invalid regular expression.",
	E017: "Unclosed comment.",
	E018: "Unbegun comment.",
	E019: "Unmatched '{a}'.",
	E020: "Expected '{a}' to match '{b}' from line {c} and instead saw '{d}'.",
	E021: "Expected '{a}' and instead saw '{b}'.",
	E022: "Line breaking error '{a}'.",
	E023: "Missing '{a}'.",
	E024: "Unexpected '{a}'.",
	E025: "Missing ':' on a case clause.",
	E026: "Missing '}' to match '{' from line {a}.",
	E027: "Missing ']' to match '[' form line {a}.",
	E028: "Illegal comma.",
	E029: "Unclosed string.",
	E030: "Expected an identifier and instead saw '{a}'.",
	E031: "Bad assignment.", // FIXME: Rephrase
	E032: "Expected a small integer or 'false' and instead saw '{a}'.",
	E033: "Expected an operator and instead saw '{a}'.",
	E034: "get/set are ES5 features.",
	E035: "Missing property name.",
	E036: "Expected to see a statement and instead saw a block.",
	E037: "Constant {a} was not declared correctly.",
	E038: "Variable {a} was not declared correctly.",
	E039: "Function declarations are not invocable. Wrap the whole function invocation in parens.",
	E040: "Each value should have its own case label.",
	E041: "Unrecoverable syntax error.",
	E042: "Stopping.",
	E043: "Too many errors.",
	E044: "'{a}' is already defined and can't be redefined.",
	E045: "Invalid for each loop.",
	E046: "A yield statement shall be within a generator function (with syntax: `function*`)",
	E047: "A generator function shall contain a yield statement.",
	E048: "Let declaration not directly within block.",
	E049: "A {a} cannot be named '{b}'."
};

var warnings = {
	W001: "'hasOwnProperty' is a really bad name.",
	W002: "Value of '{a}' may be overwritten in IE 8 and earlier.",
	W003: "'{a}' was used before it was defined.",
	W004: "'{a}' is already defined.",
	W005: "A dot following a number can be confused with a decimal point.",
	W006: "Confusing minuses.",
	W007: "Confusing pluses.",
	W008: "A leading decimal point can be confused with a dot: '{a}'.",
	W009: "The array literal notation [] is preferrable.",
	W010: "The object literal notation {} is preferrable.",
	W011: "Unexpected space after '{a}'.",
	W012: "Unexpected space before '{a}'.",
	W013: "Missing space after '{a}'.",
	W014: "Bad line breaking before '{a}'.",
	W015: "Expected '{a}' to have an indentation at {b} instead at {c}.",
	W016: "Unexpected use of '{a}'.",
	W017: "Bad operand.",
	W018: "Confusing use of '{a}'.",
	W019: "Use the isNaN function to compare with NaN.",
	W020: "Read only.",
	W021: "'{a}' is a function.",
	W022: "Do not assign to the exception parameter.",
	W023: "Expected an identifier in an assignment and instead saw a function invocation.",
	W024: "Expected an identifier and instead saw '{a}' (a reserved word).",
	W025: "Missing name in function declaration.",
	W026: "Inner functions should be listed at the top of the outer function.",
	W027: "Unreachable '{a}' after '{b}'.",
	W028: "Label '{a}' on {b} statement.",
	W030: "Expected an assignment or function call and instead saw an expression.",
	W031: "Do not use 'new' for side effects.",
	W032: "Unnecessary semicolon.",
	W033: "Missing semicolon.",
	W034: "Unnecessary directive \"{a}\".",
	W035: "Empty block.",
	W036: "Unexpected /*member '{a}'.",
	W037: "'{a}' is a statement label.",
	W038: "'{a}' used out of scope.",
	W039: "'{a}' is not allowed.",
	W040: "Possible strict violation.",
	W041: "Use '{a}' to compare with '{b}'.",
	W042: "Avoid EOL escaping.",
	W043: "Bad escaping of EOL. Use option multistr if needed.",
	W044: "Bad or unnecessary escaping.",
	W045: "Bad number '{a}'.",
	W046: "Don't use extra leading zeros '{a}'.",
	W047: "A trailing decimal point can be confused with a dot: '{a}'.",
	W048: "Unexpected control character in regular expression.",
	W049: "Unexpected escaped character '{a}' in regular expression.",
	W050: "JavaScript URL.",
	W051: "Variables should not be deleted.",
	W052: "Unexpected '{a}'.",
	W053: "Do not use {a} as a constructor.",
	W054: "The Function constructor is a form of eval.",
	W055: "A constructor name should start with an uppercase letter.",
	W056: "Bad constructor.",
	W057: "Weird construction. Is 'new' unnecessary?",
	W058: "Missing '()' invoking a constructor.",
	W059: "Avoid arguments.{a}.",
	W060: "document.write can be a form of eval.",
	W061: "eval can be harmful.",
	W062: "Wrap an immediate function invocation in parens " +
		"to assist the reader in understanding that the expression " +
		"is the result of a function, and not the function itself.",
	W063: "Math is not a function.",
	W064: "Missing 'new' prefix when invoking a constructor.",
	W065: "Missing radix parameter.",
	W066: "Implied eval. Consider passing a function instead of a string.",
	W067: "Bad invocation.",
	W068: "Wrapping non-IIFE function literals in parens is unnecessary.",
	W069: "['{a}'] is better written in dot notation.",
	W070: "Extra comma. (it breaks older versions of IE)",
	W071: "This function has too many statements. ({a})",
	W072: "This function has too many parameters. ({a})",
	W073: "Blocks are nested too deeply. ({a})",
	W074: "This function's cyclomatic complexity is too high. ({a})",
	W075: "Duplicate key '{a}'.",
	W076: "Unexpected parameter '{a}' in get {b} function.",
	W077: "Expected a single parameter in set {a} function.",
	W078: "Setter is defined without getter.",
	W079: "Redefinition of '{a}'.",
	W080: "It's not necessary to initialize '{a}' to 'undefined'.",
	W081: "Too many var statements.",
	W082: "Function declarations should not be placed in blocks. " +
		"Use a function expression or move the statement to the top of " +
		"the outer function.",
	W083: "Don't make functions within a loop.",
	W084: "Assignment in conditional expression",
	W085: "Don't use 'with'.",
	W086: "Expected a 'break' statement before '{a}'.",
	W087: "Forgotten 'debugger' statement?",
	W088: "Creating global 'for' variable. Should be 'for (var {a} ...'.",
	W089: "The body of a for in should be wrapped in an if statement to filter " +
		"unwanted properties from the prototype.",
	W090: "'{a}' is not a statement label.",
	W091: "'{a}' is out of scope.",
	W092: "Wrap the /regexp/ literal in parens to disambiguate the slash operator.",
	W093: "Did you mean to return a conditional instead of an assignment?",
	W094: "Unexpected comma.",
	W095: "Expected a string and instead saw {a}.",
	W096: "The '{a}' key may produce unexpected results.",
	W097: "Use the function form of \"use strict\".",
	W098: "'{a}' is defined but never used.",
	W099: "Mixed spaces and tabs.",
	W100: "This character may get silently deleted by one or more browsers.",
	W101: "Line is too long.",
	W102: "Trailing whitespace.",
	W103: "The '{a}' property is deprecated.",
	W104: "'{a}' is only available in JavaScript 1.7.",
	W105: "Unexpected {a} in '{b}'.",
	W106: "Identifier '{a}' is not in camel case.",
	W107: "Script URL.",
	W108: "Strings must use doublequote.",
	W109: "Strings must use singlequote.",
	W110: "Mixed double and single quotes.",
	W112: "Unclosed string.",
	W113: "Control character in string: {a}.",
	W114: "Avoid {a}.",
	W115: "Octal literals are not allowed in strict mode.",
	W116: "Expected '{a}' and instead saw '{b}'.",
	W117: "'{a}' is not defined.",
	W118: "'{a}' is only available in Mozilla JavaScript extensions (use moz option).",
	W119: "'{a}' is only available in ES6 (use esnext option)."
};

var info = {
	I001: "Comma warnings can be turned off with 'laxcomma'.",
	I002: "Reserved words as properties can be used under the 'es5' option.",
	I003: "ES5 option is now set per default"
};

exports.errors = {};
exports.warnings = {};
exports.info = {};

_.each(errors, function (desc, code) {
	exports.errors[code] = { code: code, desc: desc };
});

_.each(warnings, function (desc, code) {
	exports.warnings[code] = { code: code, desc: desc };
});

_.each(info, function (desc, code) {
	exports.info[code] = { code: code, desc: desc };
});

})()
},
{"underscore":12}],
11:[function(req,module,exports){
(function(){/*
 * Lexical analysis and token construction.
 */



var _      = req("underscore");
var events = req("events");
var reg    = req("./reg.js");
var state  = req("./state.js").state;

var Token = {
	Identifier: 1,
	Punctuator: 2,
	NumericLiteral: 3,
	StringLiteral: 4,
	Comment: 5,
	Keyword: 6,
	NullLiteral: 7,
	BooleanLiteral: 8,
	RegExp: 9
};

var unicodeLetterTable = [
	170, 170, 181, 181, 186, 186, 192, 214,
	216, 246, 248, 705, 710, 721, 736, 740, 748, 748, 750, 750,
	880, 884, 886, 887, 890, 893, 902, 902, 904, 906, 908, 908,
	910, 929, 931, 1013, 1015, 1153, 1162, 1319, 1329, 1366,
	1369, 1369, 1377, 1415, 1488, 1514, 1520, 1522, 1568, 1610,
	1646, 1647, 1649, 1747, 1749, 1749, 1765, 1766, 1774, 1775,
	1786, 1788, 1791, 1791, 1808, 1808, 1810, 1839, 1869, 1957,
	1969, 1969, 1994, 2026, 2036, 2037, 2042, 2042, 2048, 2069,
	2074, 2074, 2084, 2084, 2088, 2088, 2112, 2136, 2308, 2361,
	2365, 2365, 2384, 2384, 2392, 2401, 2417, 2423, 2425, 2431,
	2437, 2444, 2447, 2448, 2451, 2472, 2474, 2480, 2482, 2482,
	2486, 2489, 2493, 2493, 2510, 2510, 2524, 2525, 2527, 2529,
	2544, 2545, 2565, 2570, 2575, 2576, 2579, 2600, 2602, 2608,
	2610, 2611, 2613, 2614, 2616, 2617, 2649, 2652, 2654, 2654,
	2674, 2676, 2693, 2701, 2703, 2705, 2707, 2728, 2730, 2736,
	2738, 2739, 2741, 2745, 2749, 2749, 2768, 2768, 2784, 2785,
	2821, 2828, 2831, 2832, 2835, 2856, 2858, 2864, 2866, 2867,
	2869, 2873, 2877, 2877, 2908, 2909, 2911, 2913, 2929, 2929,
	2947, 2947, 2949, 2954, 2958, 2960, 2962, 2965, 2969, 2970,
	2972, 2972, 2974, 2975, 2979, 2980, 2984, 2986, 2990, 3001,
	3024, 3024, 3077, 3084, 3086, 3088, 3090, 3112, 3114, 3123,
	3125, 3129, 3133, 3133, 3160, 3161, 3168, 3169, 3205, 3212,
	3214, 3216, 3218, 3240, 3242, 3251, 3253, 3257, 3261, 3261,
	3294, 3294, 3296, 3297, 3313, 3314, 3333, 3340, 3342, 3344,
	3346, 3386, 3389, 3389, 3406, 3406, 3424, 3425, 3450, 3455,
	3461, 3478, 3482, 3505, 3507, 3515, 3517, 3517, 3520, 3526,
	3585, 3632, 3634, 3635, 3648, 3654, 3713, 3714, 3716, 3716,
	3719, 3720, 3722, 3722, 3725, 3725, 3732, 3735, 3737, 3743,
	3745, 3747, 3749, 3749, 3751, 3751, 3754, 3755, 3757, 3760,
	3762, 3763, 3773, 3773, 3776, 3780, 3782, 3782, 3804, 3805,
	3840, 3840, 3904, 3911, 3913, 3948, 3976, 3980, 4096, 4138,
	4159, 4159, 4176, 4181, 4186, 4189, 4193, 4193, 4197, 4198,
	4206, 4208, 4213, 4225, 4238, 4238, 4256, 4293, 4304, 4346,
	4348, 4348, 4352, 4680, 4682, 4685, 4688, 4694, 4696, 4696,
	4698, 4701, 4704, 4744, 4746, 4749, 4752, 4784, 4786, 4789,
	4792, 4798, 4800, 4800, 4802, 4805, 4808, 4822, 4824, 4880,
	4882, 4885, 4888, 4954, 4992, 5007, 5024, 5108, 5121, 5740,
	5743, 5759, 5761, 5786, 5792, 5866, 5870, 5872, 5888, 5900,
	5902, 5905, 5920, 5937, 5952, 5969, 5984, 5996, 5998, 6000,
	6016, 6067, 6103, 6103, 6108, 6108, 6176, 6263, 6272, 6312,
	6314, 6314, 6320, 6389, 6400, 6428, 6480, 6509, 6512, 6516,
	6528, 6571, 6593, 6599, 6656, 6678, 6688, 6740, 6823, 6823,
	6917, 6963, 6981, 6987, 7043, 7072, 7086, 7087, 7104, 7141,
	7168, 7203, 7245, 7247, 7258, 7293, 7401, 7404, 7406, 7409,
	7424, 7615, 7680, 7957, 7960, 7965, 7968, 8005, 8008, 8013,
	8016, 8023, 8025, 8025, 8027, 8027, 8029, 8029, 8031, 8061,
	8064, 8116, 8118, 8124, 8126, 8126, 8130, 8132, 8134, 8140,
	8144, 8147, 8150, 8155, 8160, 8172, 8178, 8180, 8182, 8188,
	8305, 8305, 8319, 8319, 8336, 8348, 8450, 8450, 8455, 8455,
	8458, 8467, 8469, 8469, 8473, 8477, 8484, 8484, 8486, 8486,
	8488, 8488, 8490, 8493, 8495, 8505, 8508, 8511, 8517, 8521,
	8526, 8526, 8544, 8584, 11264, 11310, 11312, 11358,
	11360, 11492, 11499, 11502, 11520, 11557, 11568, 11621,
	11631, 11631, 11648, 11670, 11680, 11686, 11688, 11694,
	11696, 11702, 11704, 11710, 11712, 11718, 11720, 11726,
	11728, 11734, 11736, 11742, 11823, 11823, 12293, 12295,
	12321, 12329, 12337, 12341, 12344, 12348, 12353, 12438,
	12445, 12447, 12449, 12538, 12540, 12543, 12549, 12589,
	12593, 12686, 12704, 12730, 12784, 12799, 13312, 13312,
	19893, 19893, 19968, 19968, 40907, 40907, 40960, 42124,
	42192, 42237, 42240, 42508, 42512, 42527, 42538, 42539,
	42560, 42606, 42623, 42647, 42656, 42735, 42775, 42783,
	42786, 42888, 42891, 42894, 42896, 42897, 42912, 42921,
	43002, 43009, 43011, 43013, 43015, 43018, 43020, 43042,
	43072, 43123, 43138, 43187, 43250, 43255, 43259, 43259,
	43274, 43301, 43312, 43334, 43360, 43388, 43396, 43442,
	43471, 43471, 43520, 43560, 43584, 43586, 43588, 43595,
	43616, 43638, 43642, 43642, 43648, 43695, 43697, 43697,
	43701, 43702, 43705, 43709, 43712, 43712, 43714, 43714,
	43739, 43741, 43777, 43782, 43785, 43790, 43793, 43798,
	43808, 43814, 43816, 43822, 43968, 44002, 44032, 44032,
	55203, 55203, 55216, 55238, 55243, 55291, 63744, 64045,
	64048, 64109, 64112, 64217, 64256, 64262, 64275, 64279,
	64285, 64285, 64287, 64296, 64298, 64310, 64312, 64316,
	64318, 64318, 64320, 64321, 64323, 64324, 64326, 64433,
	64467, 64829, 64848, 64911, 64914, 64967, 65008, 65019,
	65136, 65140, 65142, 65276, 65313, 65338, 65345, 65370,
	65382, 65470, 65474, 65479, 65482, 65487, 65490, 65495,
	65498, 65500, 65536, 65547, 65549, 65574, 65576, 65594,
	65596, 65597, 65599, 65613, 65616, 65629, 65664, 65786,
	65856, 65908, 66176, 66204, 66208, 66256, 66304, 66334,
	66352, 66378, 66432, 66461, 66464, 66499, 66504, 66511,
	66513, 66517, 66560, 66717, 67584, 67589, 67592, 67592,
	67594, 67637, 67639, 67640, 67644, 67644, 67647, 67669,
	67840, 67861, 67872, 67897, 68096, 68096, 68112, 68115,
	68117, 68119, 68121, 68147, 68192, 68220, 68352, 68405,
	68416, 68437, 68448, 68466, 68608, 68680, 69635, 69687,
	69763, 69807, 73728, 74606, 74752, 74850, 77824, 78894,
	92160, 92728, 110592, 110593, 119808, 119892, 119894, 119964,
	119966, 119967, 119970, 119970, 119973, 119974, 119977, 119980,
	119982, 119993, 119995, 119995, 119997, 120003, 120005, 120069,
	120071, 120074, 120077, 120084, 120086, 120092, 120094, 120121,
	120123, 120126, 120128, 120132, 120134, 120134, 120138, 120144,
	120146, 120485, 120488, 120512, 120514, 120538, 120540, 120570,
	120572, 120596, 120598, 120628, 120630, 120654, 120656, 120686,
	120688, 120712, 120714, 120744, 120746, 120770, 120772, 120779,
	131072, 131072, 173782, 173782, 173824, 173824, 177972, 177972,
	177984, 177984, 178205, 178205, 194560, 195101
];

var identifierStartTable = [];

for (var i = 0; i < 128; i++) {
	identifierStartTable[i] =
		i === 36 ||           // $
		i >= 65 && i <= 90 || // A-Z
		i === 95 ||           // _
		i >= 97 && i <= 122;  // a-z
}

var identifierPartTable = [];

for (var i = 0; i < 128; i++) {
	identifierPartTable[i] =
		identifierStartTable[i] || // $, _, A-Z, a-z
		i >= 48 && i <= 57;        // 0-9
}

function asyncTrigger() {
	var _checks = [];

	return {
		push: function (fn) {
			_checks.push(fn);
		},

		check: function () {
			for (var check = 0; check < _checks.length; ++check) {
				_checks[check]();
			}

			_checks.splice(0, _checks.length);
		}
	};
}
function Lexer(source) {
	var lines = source;

	if (typeof lines === "string") {
		lines = lines
			.replace(/\r\n/g, "\n")
			.replace(/\r/g, "\n")
			.split("\n");
	}

	if (lines[0] && lines[0].substr(0, 2) === "#!") {
		lines[0] = "";
	}

	this.emitter = new events.EventEmitter();
	this.source = source;
	this.setLines(lines);
	this.prereg = true;

	this.line = 0;
	this.char = 1;
	this.from = 1;
	this.input = "";

	for (var i = 0; i < state.option.indent; i += 1) {
		state.tab += " ";
	}
}

Lexer.prototype = {
	_lines: [],

	getLines: function () {
		this._lines = state.lines;
		return this._lines;
	},

	setLines: function (val) {
		this._lines = val;
		state.lines = this._lines;
	},
	peek: function (i) {
		return this.input.charAt(i || 0);
	},
	skip: function (i) {
		i = i || 1;
		this.char += i;
		this.input = this.input.slice(i);
	},
	on: function (names, listener) {
		names.split(" ").forEach(function (name) {
			this.emitter.on(name, listener);
		}.bind(this));
	},
	trigger: function () {
		this.emitter.emit.apply(this.emitter, Array.prototype.slice.call(arguments));
	},
	triggerAsync: function (type, args, checks, fn) {
		checks.push(function () {
			if (fn()) {
				this.trigger(type, args);
			}
		}.bind(this));
	},
	scanPunctuator: function () {
		var ch1 = this.peek();
		var ch2, ch3, ch4;

		switch (ch1) {
		case ".":
			if ((/^[0-9]$/).test(this.peek(1))) {
				return null;
			}
			if (this.peek(1) === "." && this.peek(2) === ".") {
				return {
					type: Token.Punctuator,
					value: "..."
				};
			}
		case "(":
		case ")":
		case ";":
		case ",":
		case "{":
		case "}":
		case "[":
		case "]":
		case ":":
		case "~":
		case "?":
			return {
				type: Token.Punctuator,
				value: ch1
			};
		case "#":
			return {
				type: Token.Punctuator,
				value: ch1
			};
		case "":
			return null;
		}

		ch2 = this.peek(1);
		ch3 = this.peek(2);
		ch4 = this.peek(3);

		if (ch1 === ">" && ch2 === ">" && ch3 === ">" && ch4 === "=") {
			return {
				type: Token.Punctuator,
				value: ">>>="
			};
		}

		if (ch1 === "=" && ch2 === "=" && ch3 === "=") {
			return {
				type: Token.Punctuator,
				value: "==="
			};
		}

		if (ch1 === "!" && ch2 === "=" && ch3 === "=") {
			return {
				type: Token.Punctuator,
				value: "!=="
			};
		}

		if (ch1 === ">" && ch2 === ">" && ch3 === ">") {
			return {
				type: Token.Punctuator,
				value: ">>>"
			};
		}

		if (ch1 === "<" && ch2 === "<" && ch3 === "=") {
			return {
				type: Token.Punctuator,
				value: "<<="
			};
		}

		if (ch1 === ">" && ch2 === ">" && ch3 === "=") {
			return {
				type: Token.Punctuator,
				value: ">>="
			};
		}
		if (ch1 === "=" && ch2 === ">") {
			return {
				type: Token.Punctuator,
				value: ch1 + ch2
			};
		}
		if (ch1 === ch2 && ("+-<>&|".indexOf(ch1) >= 0)) {
			return {
				type: Token.Punctuator,
				value: ch1 + ch2
			};
		}

		if ("<>=!+-*%&|^".indexOf(ch1) >= 0) {
			if (ch2 === "=") {
				return {
					type: Token.Punctuator,
					value: ch1 + ch2
				};
			}

			return {
				type: Token.Punctuator,
				value: ch1
			};
		}

		if (ch1 === "/") {
			if (ch2 === "=" && /\/=(?!(\S*\/[gim]?))/.test(this.input)) {
				return {
					type: Token.Punctuator,
					value: "/="
				};
			}

			return {
				type: Token.Punctuator,
				value: "/"
			};
		}

		return null;
	},
	scanComments: function () {
		var ch1 = this.peek();
		var ch2 = this.peek(1);
		var rest = this.input.substr(2);
		var startLine = this.line;
		var startChar = this.char;

		function commentToken(label, body, opt) {
			var special = ["jshint", "jslint", "members", "member", "globals", "global", "exported"];
			var isSpecial = false;
			var value = label + body;
			var commentType = "plain";
			opt = opt || {};

			if (opt.isMultiline) {
				value += "*/";
			}

			special.forEach(function (str) {
				if (isSpecial) {
					return;
				}
				if (label === "//" && str !== "jshint") {
					return;
				}

				if (body.substr(0, str.length) === str) {
					isSpecial = true;
					label = label + str;
					body = body.substr(str.length);
				}

				if (!isSpecial && body.charAt(0) === " " && body.substr(1, str.length) === str) {
					isSpecial = true;
					label = label + " " + str;
					body = body.substr(str.length + 1);
				}

				if (!isSpecial) {
					return;
				}

				switch (str) {
				case "member":
					commentType = "members";
					break;
				case "global":
					commentType = "globals";
					break;
				default:
					commentType = str;
				}
			});

			return {
				type: Token.Comment,
				commentType: commentType,
				value: value,
				body: body,
				isSpecial: isSpecial,
				isMultiline: opt.isMultiline || false,
				isMalformed: opt.isMalformed || false
			};
		}
		if (ch1 === "*" && ch2 === "/") {
			this.trigger("error", {
				code: "E018",
				line: startLine,
				character: startChar
			});

			this.skip(2);
			return null;
		}
		if (ch1 !== "/" || (ch2 !== "*" && ch2 !== "/")) {
			return null;
		}
		if (ch2 === "/") {
			this.skip(this.input.length); // Skip to the EOL.
			return commentToken("//", rest);
		}

		var body = "";
		if (ch2 === "*") {
			this.skip(2);

			while (this.peek() !== "*" || this.peek(1) !== "/") {
				if (this.peek() === "") { // End of Line
					body += "\n";
					if (!this.nextLine()) {
						this.trigger("error", {
							code: "E017",
							line: startLine,
							character: startChar
						});

						return commentToken("/*", body, {
							isMultiline: true,
							isMalformed: true
						});
					}
				} else {
					body += this.peek();
					this.skip();
				}
			}

			this.skip(2);
			return commentToken("/*", body, { isMultiline: true });
		}
	},
	scanKeyword: function () {
		var result = /^[a-zA-Z_$][a-zA-Z0-9_$]*/.exec(this.input);
		var keywords = [
			"if", "in", "do", "var", "for", "new",
			"try", "let", "this", "else", "case",
			"void", "with", "enum", "while", "break",
			"catch", "throw", "const", "yield", "class",
			"super", "return", "typeof", "delete",
			"switch", "export", "import", "default",
			"finally", "extends", "function", "continue",
			"debugger", "instanceof"
		];

		if (result && keywords.indexOf(result[0]) >= 0) {
			return {
				type: Token.Keyword,
				value: result[0]
			};
		}

		return null;
	},
	scanIdentifier: function () {
		var id = "";
		var index = 0;
		var type, char;

		function isUnicodeLetter(code) {
			for (var i = 0; i < unicodeLetterTable.length;) {
				if (code < unicodeLetterTable[i++]) {
					return false;
				}

				if (code <= unicodeLetterTable[i++]) {
					return true;
				}
			}

			return false;
		}

		function isHexDigit(str) {
			return (/^[0-9a-fA-F]$/).test(str);
		}

		var readUnicodeEscapeSequence = function () {
			index += 1;

			if (this.peek(index) !== "u") {
				return null;
			}

			var ch1 = this.peek(index + 1);
			var ch2 = this.peek(index + 2);
			var ch3 = this.peek(index + 3);
			var ch4 = this.peek(index + 4);
			var code;

			if (isHexDigit(ch1) && isHexDigit(ch2) && isHexDigit(ch3) && isHexDigit(ch4)) {
				code = parseInt(ch1 + ch2 + ch3 + ch4, 16);

				if (isUnicodeLetter(code)) {
					index += 5;
					return "\\u" + ch1 + ch2 + ch3 + ch4;
				}

				return null;
			}

			return null;
		}.bind(this);

		var getIdentifierStart = function () {
			var chr = this.peek(index);
			var code = chr.charCodeAt(0);

			if (code === 92) {
				return readUnicodeEscapeSequence();
			}

			if (code < 128) {
				if (identifierStartTable[code]) {
					index += 1;
					return chr;
				}

				return null;
			}

			if (isUnicodeLetter(code)) {
				index += 1;
				return chr;
			}

			return null;
		}.bind(this);

		var getIdentifierPart = function () {
			var chr = this.peek(index);
			var code = chr.charCodeAt(0);

			if (code === 92) {
				return readUnicodeEscapeSequence();
			}

			if (code < 128) {
				if (identifierPartTable[code]) {
					index += 1;
					return chr;
				}

				return null;
			}

			if (isUnicodeLetter(code)) {
				index += 1;
				return chr;
			}

			return null;
		}.bind(this);

		char = getIdentifierStart();
		if (char === null) {
			return null;
		}

		id = char;
		for (;;) {
			char = getIdentifierPart();

			if (char === null) {
				break;
			}

			id += char;
		}

		switch (id) {
		case "true":
		case "false":
			type = Token.BooleanLiteral;
			break;
		case "null":
			type = Token.NullLiteral;
			break;
		default:
			type = Token.Identifier;
		}

		return {
			type: type,
			value: id
		};
	},
	scanNumericLiteral: function () {
		var index = 0;
		var value = "";
		var length = this.input.length;
		var char = this.peek(index);
		var bad;

		function isDecimalDigit(str) {
			return (/^[0-9]$/).test(str);
		}

		function isOctalDigit(str) {
			return (/^[0-7]$/).test(str);
		}

		function isHexDigit(str) {
			return (/^[0-9a-fA-F]$/).test(str);
		}

		function isIdentifierStart(ch) {
			return (ch === "$") || (ch === "_") || (ch === "\\") ||
				(ch >= "a" && ch <= "z") || (ch >= "A" && ch <= "Z");
		}

		if (char !== "." && !isDecimalDigit(char)) {
			return null;
		}

		if (char !== ".") {
			value = this.peek(index);
			index += 1;
			char = this.peek(index);

			if (value === "0") {
				if (char === "x" || char === "X") {
					index += 1;
					value += char;

					while (index < length) {
						char = this.peek(index);
						if (!isHexDigit(char)) {
							break;
						}
						value += char;
						index += 1;
					}

					if (value.length <= 2) { // 0x
						return {
							type: Token.NumericLiteral,
							value: value,
							isMalformed: true
						};
					}

					if (index < length) {
						char = this.peek(index);
						if (isIdentifierStart(char)) {
							return null;
						}
					}

					return {
						type: Token.NumericLiteral,
						value: value,
						base: 16,
						isMalformed: false
					};
				}
				if (isOctalDigit(char)) {
					index += 1;
					value += char;
					bad = false;

					while (index < length) {
						char = this.peek(index);

						if (isDecimalDigit(char)) {
							bad = true;
						} else if (!isOctalDigit(char)) {
							break;
						}
						value += char;
						index += 1;
					}

					if (index < length) {
						char = this.peek(index);
						if (isIdentifierStart(char)) {
							return null;
						}
					}

					return {
						type: Token.NumericLiteral,
						value: value,
						base: 8,
						isMalformed: false
					};
				}

				if (isDecimalDigit(char)) {
					index += 1;
					value += char;
				}
			}

			while (index < length) {
				char = this.peek(index);
				if (!isDecimalDigit(char)) {
					break;
				}
				value += char;
				index += 1;
			}
		}

		if (char === ".") {
			value += char;
			index += 1;

			while (index < length) {
				char = this.peek(index);
				if (!isDecimalDigit(char)) {
					break;
				}
				value += char;
				index += 1;
			}
		}

		if (char === "e" || char === "E") {
			value += char;
			index += 1;
			char = this.peek(index);

			if (char === "+" || char === "-") {
				value += this.peek(index);
				index += 1;
			}

			char = this.peek(index);
			if (isDecimalDigit(char)) {
				value += char;
				index += 1;

				while (index < length) {
					char = this.peek(index);
					if (!isDecimalDigit(char)) {
						break;
					}
					value += char;
					index += 1;
				}
			} else {
				return null;
			}
		}

		if (index < length) {
			char = this.peek(index);
			if (isIdentifierStart(char)) {
				return null;
			}
		}

		return {
			type: Token.NumericLiteral,
			value: value,
			base: 10,
			isMalformed: !isFinite(value)
		};
	},
	scanStringLiteral: function (checks) {
		var quote = this.peek();
		if (quote !== "\"" && quote !== "'") {
			return null;
		}
		this.triggerAsync("warning", {
			code: "W108",
			line: this.line,
			character: this.char // +1?
		}, checks, function () { return state.jsonMode && quote !== "\""; });

		var value = "";
		var startLine = this.line;
		var startChar = this.char;
		var allowNewLine = false;

		this.skip();

		while (this.peek() !== quote) {
			while (this.peek() === "") { // End Of Line

				if (!allowNewLine) {
					this.trigger("warning", {
						code: "W112",
						line: this.line,
						character: this.char
					});
				} else {
					allowNewLine = false;

					this.triggerAsync("warning", {
						code: "W043",
						line: this.line,
						character: this.char
					}, checks, function () { return !state.option.multistr; });

					this.triggerAsync("warning", {
						code: "W042",
						line: this.line,
						character: this.char
					}, checks, function () { return state.jsonMode && state.option.multistr; });
				}

				if (!this.nextLine()) {
					this.trigger("error", {
						code: "E029",
						line: startLine,
						character: startChar
					});

					return {
						type: Token.StringLiteral,
						value: value,
						isUnclosed: true,
						quote: quote
					};
				}
			}

			allowNewLine = false;
			var char = this.peek();
			var jump = 1; // A length of a jump, after we're done

			if (char < " ") {
				this.trigger("warning", {
					code: "W113",
					line: this.line,
					character: this.char,
					data: [ "<non-printable>" ]
				});
			}

			if (char === "\\") {
				this.skip();
				char = this.peek();

				switch (char) {
				case "'":
					this.triggerAsync("warning", {
						code: "W114",
						line: this.line,
						character: this.char,
						data: [ "\\'" ]
					}, checks, function () {return state.jsonMode; });
					break;
				case "b":
					char = "\b";
					break;
				case "f":
					char = "\f";
					break;
				case "n":
					char = "\n";
					break;
				case "r":
					char = "\r";
					break;
				case "t":
					char = "\t";
					break;
				case "0":
					char = "\0";
					var n = parseInt(this.peek(1), 10);
					this.triggerAsync("warning", {
						code: "W115",
						line: this.line,
						character: this.char
					}, checks,
					function () { return n >= 0 && n <= 7 && state.directive["use strict"]; });
					break;
				case "u":
					char = String.fromCharCode(parseInt(this.input.substr(1, 4), 16));
					jump = 5;
					break;
				case "v":
					this.triggerAsync("warning", {
						code: "W114",
						line: this.line,
						character: this.char,
						data: [ "\\v" ]
					}, checks, function () { return state.jsonMode; });

					char = "\v";
					break;
				case "x":
					var	x = parseInt(this.input.substr(1, 2), 16);

					this.triggerAsync("warning", {
						code: "W114",
						line: this.line,
						character: this.char,
						data: [ "\\x-" ]
					}, checks, function () { return state.jsonMode; });

					char = String.fromCharCode(x);
					jump = 3;
					break;
				case "\\":
				case "\"":
				case "/":
					break;
				case "":
					allowNewLine = true;
					char = "";
					break;
				case "!":
					if (value.slice(value.length - 2) === "<") {
						break;
					}
				default:
					this.trigger("warning", {
						code: "W044",
						line: this.line,
						character: this.char
					});
				}
			}

			value += char;
			this.skip(jump);
		}

		this.skip();
		return {
			type: Token.StringLiteral,
			value: value,
			isUnclosed: false,
			quote: quote
		};
	},
	scanRegExp: function () {
		var index = 0;
		var length = this.input.length;
		var char = this.peek();
		var value = char;
		var body = "";
		var flags = [];
		var malformed = false;
		var isCharSet = false;
		var terminated;

		var scanUnexpectedChars = function () {
			if (char < " ") {
				malformed = true;
				this.trigger("warning", {
					code: "W048",
					line: this.line,
					character: this.char
				});
			}
			if (char === "<") {
				malformed = true;
				this.trigger("warning", {
					code: "W049",
					line: this.line,
					character: this.char,
					data: [ char ]
				});
			}
		}.bind(this);
		if (!this.prereg || char !== "/") {
			return null;
		}

		index += 1;
		terminated = false;

		while (index < length) {
			char = this.peek(index);
			value += char;
			body += char;

			if (isCharSet) {
				if (char === "]") {
					if (this.peek(index - 1) !== "\\" || this.peek(index - 2) === "\\") {
						isCharSet = false;
					}
				}

				if (char === "\\") {
					index += 1;
					char = this.peek(index);
					body += char;
					value += char;

					scanUnexpectedChars();
				}

				index += 1;
				continue;
			}

			if (char === "\\") {
				index += 1;
				char = this.peek(index);
				body += char;
				value += char;

				scanUnexpectedChars();

				if (char === "/") {
					index += 1;
					continue;
				}

				if (char === "[") {
					index += 1;
					continue;
				}
			}

			if (char === "[") {
				isCharSet = true;
				index += 1;
				continue;
			}

			if (char === "/") {
				body = body.substr(0, body.length - 1);
				terminated = true;
				index += 1;
				break;
			}

			index += 1;
		}

		if (!terminated) {
			this.trigger("error", {
				code: "E015",
				line: this.line,
				character: this.from
			});

			return void this.trigger("fatal", {
				line: this.line,
				from: this.from
			});
		}

		while (index < length) {
			char = this.peek(index);
			if (!/[gim]/.test(char)) {
				break;
			}
			flags.push(char);
			value += char;
			index += 1;
		}

		try {
			new RegExp(body, flags.join(""));
		} catch (err) {
			malformed = true;
			this.trigger("error", {
				code: "E016",
				line: this.line,
				character: this.char,
				data: [ err.message ] // Platform dependent!
			});
		}

		return {
			type: Token.RegExp,
			value: value,
			flags: flags,
			isMalformed: malformed
		};
	},
	scanMixedSpacesAndTabs: function () {
		var at, match;

		if (state.option.smarttabs) {
			match = this.input.match(/(\/\/|^\s?\*)? \t/);
			at = match && !match[1] ? 0 : -1;
		} else {
			at = this.input.search(/ \t|\t [^\*]/);
		}

		return at;
	},
	scanUnsafeChars: function () {
		return this.input.search(reg.unsafeChars);
	},
	next: function (checks) {
		this.from = this.char;
		var start;
		if (/\s/.test(this.peek())) {
			start = this.char;

			while (/\s/.test(this.peek())) {
				this.from += 1;
				this.skip();
			}

			if (this.peek() === "") { // EOL
				if (!/^\s*$/.test(this.getLines()[this.line - 1]) && state.option.trailing) {
					this.trigger("warning", { code: "W102", line: this.line, character: start });
				}
			}
		}

		var match = this.scanComments() ||
			this.scanStringLiteral(checks);

		if (match) {
			return match;
		}

		match =
			this.scanRegExp() ||
			this.scanPunctuator() ||
			this.scanKeyword() ||
			this.scanIdentifier() ||
			this.scanNumericLiteral();

		if (match) {
			this.skip(match.value.length);
			return match;
		}

		return null;
	},
	nextLine: function () {
		var char;

		if (this.line >= this.getLines().length) {
			return false;
		}

		this.input = this.getLines()[this.line];
		this.line += 1;
		this.char = 1;
		this.from = 1;

		char = this.scanMixedSpacesAndTabs();
		if (char >= 0) {
			this.trigger("warning", { code: "W099", line: this.line, character: char + 1 });
		}

		this.input = this.input.replace(/\t/g, state.tab);
		char = this.scanUnsafeChars();

		if (char >= 0) {
			this.trigger("warning", { code: "W100", line: this.line, character: char });
		}

		if (state.option.maxlen && state.option.maxlen < this.input.length) {
			this.trigger("warning", { code: "W101", line: this.line, character: this.input.length });
		}

		return true;
	},
	start: function () {
		this.nextLine();
	},
	token: function () {
		var checks = asyncTrigger();
		var token;


		function isReserved(token, isProperty) {
			if (!token.reserved) {
				return false;
			}

			if (token.meta && token.meta.isFutureReservedWord) {
				if (state.option.inES5(true) && !token.meta.es5) {
					return false;
				}
				if (token.meta.strictOnly) {
					if (!state.option.strict && !state.directive["use strict"]) {
						return false;
					}
				}

				if (isProperty) {
					return false;
				}
			}

			return true;
		}
		var create = function (type, value, isProperty) {
			var obj;

			if (type !== "(endline)" && type !== "(end)") {
				this.prereg = false;
			}

			if (type === "(punctuator)") {
				switch (value) {
				case ".":
				case ")":
				case "~":
				case "#":
				case "]":
					this.prereg = false;
					break;
				default:
					this.prereg = true;
				}

				obj = Object.create(state.syntax[value] || state.syntax["(error)"]);
			}

			if (type === "(identifier)") {
				if (value === "return" || value === "case" || value === "typeof") {
					this.prereg = true;
				}

				if (_.has(state.syntax, value)) {
					obj = Object.create(state.syntax[value] || state.syntax["(error)"]);
					if (!isReserved(obj, isProperty && type === "(identifier)")) {
						obj = null;
					}
				}
			}

			if (!obj) {
				obj = Object.create(state.syntax[type]);
			}

			obj.identifier = (type === "(identifier)");
			obj.type = obj.type || type;
			obj.value = value;
			obj.line = this.line;
			obj.character = this.char;
			obj.from = this.from;

			if (isProperty && obj.identifier) {
				obj.isProperty = isProperty;
			}

			obj.check = checks.check;

			return obj;
		}.bind(this);

		for (;;) {
			if (!this.input.length) {
				return create(this.nextLine() ? "(endline)" : "(end)", "");
			}

			token = this.next(checks);

			if (!token) {
				if (this.input.length) {
					this.trigger("error", {
						code: "E024",
						line: this.line,
						character: this.char,
						data: [ this.peek() ]
					});

					this.input = "";
				}

				continue;
			}

			switch (token.type) {
			case Token.StringLiteral:
				this.triggerAsync("String", {
					line: this.line,
					char: this.char,
					from: this.from,
					value: token.value,
					quote: token.quote
				}, checks, function () { return true; });

				return create("(string)", token.value);
			case Token.Identifier:
				this.trigger("Identifier", {
					line: this.line,
					char: this.char,
					from: this.form,
					name: token.value,
					isProperty: state.tokens.curr.id === "."
				});
			case Token.Keyword:
			case Token.NullLiteral:
			case Token.BooleanLiteral:
				return create("(identifier)", token.value, state.tokens.curr.id === ".");

			case Token.NumericLiteral:
				if (token.isMalformed) {
					this.trigger("warning", {
						code: "W045",
						line: this.line,
						character: this.char,
						data: [ token.value ]
					});
				}

				this.triggerAsync("warning", {
					code: "W114",
					line: this.line,
					character: this.char,
					data: [ "0x-" ]
				}, checks, function () { return token.base === 16 && state.jsonMode; });

				this.triggerAsync("warning", {
					code: "W115",
					line: this.line,
					character: this.char
				}, checks, function () {
					return state.directive["use strict"] && token.base === 8; 
				});

				this.trigger("Number", {
					line: this.line,
					char: this.char,
					from: this.from,
					value: token.value,
					base: token.base,
					isMalformed: token.malformed
				});

				return create("(number)", token.value);

			case Token.RegExp:
				return create("(regexp)", token.value);

			case Token.Comment:
				state.tokens.curr.comment = true;

				if (token.isSpecial) {
					return {
						value: token.value,
						body: token.body,
						type: token.commentType,
						isSpecial: token.isSpecial,
						line: this.line,
						character: this.char,
						from: this.from
					};
				}

				break;

			case "":
				break;

			default:
				return create("(punctuator)", token.value);
			}
		}
	}
};

exports.Lexer = Lexer;

})()
},
{"events":2,"./reg.js":4,"./state.js":5,"underscore":12}],
12:[function(req,module,exports){
(function(){//     Underscore.js 1.4.4

(function() {
  var root = this;
  var previousUnderscore = root._;
  var breaker = {};
  var ArrayProto = Array.prototype, ObjProto = Object.prototype, FuncProto = Function.prototype;
  var push             = ArrayProto.push,
      slice            = ArrayProto.slice,
      concat           = ArrayProto.concat,
      toString         = ObjProto.toString,
      hasOwnProperty   = ObjProto.hasOwnProperty;
  var
    nativeForEach      = ArrayProto.forEach,
    nativeMap          = ArrayProto.map,
    nativeReduce       = ArrayProto.reduce,
    nativeReduceRight  = ArrayProto.reduceRight,
    nativeFilter       = ArrayProto.filter,
    nativeEvery        = ArrayProto.every,
    nativeSome         = ArrayProto.some,
    nativeIndexOf      = ArrayProto.indexOf,
    nativeLastIndexOf  = ArrayProto.lastIndexOf,
    nativeIsArray      = Array.isArray,
    nativeKeys         = Object.keys,
    nativeBind         = FuncProto.bind;
  var _ = function(obj) {
    if (obj instanceof _) return obj;
    if (!(this instanceof _)) return new _(obj);
    this._wrapped = obj;
  };
  if (typeof exports !== 'undefined') {
    if (typeof module !== 'undefined' && module.exports) {
      exports = module.exports = _;
    }
    exports._ = _;
  } else {
    root._ = _;
  }
  _.VERSION = '1.4.4';
  var each = _.each = _.forEach = function(obj, iterator, context) {
    if (obj == null) return;
    if (nativeForEach && obj.forEach === nativeForEach) {
      obj.forEach(iterator, context);
    } else if (obj.length === +obj.length) {
      for (var i = 0, l = obj.length; i < l; i++) {
        if (iterator.call(context, obj[i], i, obj) === breaker) return;
      }
    } else {
      for (var key in obj) {
        if (_.has(obj, key)) {
          if (iterator.call(context, obj[key], key, obj) === breaker) return;
        }
      }
    }
  };
  _.map = _.collect = function(obj, iterator, context) {
    var results = [];
    if (obj == null) return results;
    if (nativeMap && obj.map === nativeMap) return obj.map(iterator, context);
    each(obj, function(value, index, list) {
      results[results.length] = iterator.call(context, value, index, list);
    });
    return results;
  };

  var reduceError = 'Reduce of empty array with no initial value';
  _.reduce = _.foldl = _.inject = function(obj, iterator, memo, context) {
    var initial = arguments.length > 2;
    if (obj == null) obj = [];
    if (nativeReduce && obj.reduce === nativeReduce) {
      if (context) iterator = _.bind(iterator, context);
      return initial ? obj.reduce(iterator, memo) : obj.reduce(iterator);
    }
    each(obj, function(value, index, list) {
      if (!initial) {
        memo = value;
        initial = true;
      } else {
        memo = iterator.call(context, memo, value, index, list);
      }
    });
    if (!initial) throw new TypeError(reduceError);
    return memo;
  };
  _.reduceRight = _.foldr = function(obj, iterator, memo, context) {
    var initial = arguments.length > 2;
    if (obj == null) obj = [];
    if (nativeReduceRight && obj.reduceRight === nativeReduceRight) {
      if (context) iterator = _.bind(iterator, context);
      return initial ? obj.reduceRight(iterator, memo) : obj.reduceRight(iterator);
    }
    var length = obj.length;
    if (length !== +length) {
      var keys = _.keys(obj);
      length = keys.length;
    }
    each(obj, function(value, index, list) {
      index = keys ? keys[--length] : --length;
      if (!initial) {
        memo = obj[index];
        initial = true;
      } else {
        memo = iterator.call(context, memo, obj[index], index, list);
      }
    });
    if (!initial) throw new TypeError(reduceError);
    return memo;
  };
  _.find = _.detect = function(obj, iterator, context) {
    var result;
    any(obj, function(value, index, list) {
      if (iterator.call(context, value, index, list)) {
        result = value;
        return true;
      }
    });
    return result;
  };
  _.filter = _.select = function(obj, iterator, context) {
    var results = [];
    if (obj == null) return results;
    if (nativeFilter && obj.filter === nativeFilter) return obj.filter(iterator, context);
    each(obj, function(value, index, list) {
      if (iterator.call(context, value, index, list)) results[results.length] = value;
    });
    return results;
  };
  _.reject = function(obj, iterator, context) {
    return _.filter(obj, function(value, index, list) {
      return !iterator.call(context, value, index, list);
    }, context);
  };
  _.every = _.all = function(obj, iterator, context) {
    iterator || (iterator = _.identity);
    var result = true;
    if (obj == null) return result;
    if (nativeEvery && obj.every === nativeEvery) return obj.every(iterator, context);
    each(obj, function(value, index, list) {
      if (!(result = result && iterator.call(context, value, index, list))) return breaker;
    });
    return !!result;
  };
  var any = _.some = _.any = function(obj, iterator, context) {
    iterator || (iterator = _.identity);
    var result = false;
    if (obj == null) return result;
    if (nativeSome && obj.some === nativeSome) return obj.some(iterator, context);
    each(obj, function(value, index, list) {
      if (result || (result = iterator.call(context, value, index, list))) return breaker;
    });
    return !!result;
  };
  _.contains = _.include = function(obj, target) {
    if (obj == null) return false;
    if (nativeIndexOf && obj.indexOf === nativeIndexOf) return obj.indexOf(target) != -1;
    return any(obj, function(value) {
      return value === target;
    });
  };
  _.invoke = function(obj, method) {
    var args = slice.call(arguments, 2);
    var isFunc = _.isFunction(method);
    return _.map(obj, function(value) {
      return (isFunc ? method : value[method]).apply(value, args);
    });
  };
  _.pluck = function(obj, key) {
    return _.map(obj, function(value){ return value[key]; });
  };
  _.where = function(obj, attrs, first) {
    if (_.isEmpty(attrs)) return first ? null : [];
    return _[first ? 'find' : 'filter'](obj, function(value) {
      for (var key in attrs) {
        if (attrs[key] !== value[key]) return false;
      }
      return true;
    });
  };
  _.findWhere = function(obj, attrs) {
    return _.where(obj, attrs, true);
  };
  _.max = function(obj, iterator, context) {
    if (!iterator && _.isArray(obj) && obj[0] === +obj[0] && obj.length < 65535) {
      return Math.max.apply(Math, obj);
    }
    if (!iterator && _.isEmpty(obj)) return -Infinity;
    var result = {computed : -Infinity, value: -Infinity};
    each(obj, function(value, index, list) {
      var computed = iterator ? iterator.call(context, value, index, list) : value;
      computed >= result.computed && (result = {value : value, computed : computed});
    });
    return result.value;
  };
  _.min = function(obj, iterator, context) {
    if (!iterator && _.isArray(obj) && obj[0] === +obj[0] && obj.length < 65535) {
      return Math.min.apply(Math, obj);
    }
    if (!iterator && _.isEmpty(obj)) return Infinity;
    var result = {computed : Infinity, value: Infinity};
    each(obj, function(value, index, list) {
      var computed = iterator ? iterator.call(context, value, index, list) : value;
      computed < result.computed && (result = {value : value, computed : computed});
    });
    return result.value;
  };
  _.shuffle = function(obj) {
    var rand;
    var index = 0;
    var shuffled = [];
    each(obj, function(value) {
      rand = _.random(index++);
      shuffled[index - 1] = shuffled[rand];
      shuffled[rand] = value;
    });
    return shuffled;
  };
  var lookupIterator = function(value) {
    return _.isFunction(value) ? value : function(obj){ return obj[value]; };
  };
  _.sortBy = function(obj, value, context) {
    var iterator = lookupIterator(value);
    return _.pluck(_.map(obj, function(value, index, list) {
      return {
        value : value,
        index : index,
        criteria : iterator.call(context, value, index, list)
      };
    }).sort(function(left, right) {
      var a = left.criteria;
      var b = right.criteria;
      if (a !== b) {
        if (a > b || a === void 0) return 1;
        if (a < b || b === void 0) return -1;
      }
      return left.index < right.index ? -1 : 1;
    }), 'value');
  };
  var group = function(obj, value, context, behavior) {
    var result = {};
    var iterator = lookupIterator(value || _.identity);
    each(obj, function(value, index) {
      var key = iterator.call(context, value, index, obj);
      behavior(result, key, value);
    });
    return result;
  };
  _.groupBy = function(obj, value, context) {
    return group(obj, value, context, function(result, key, value) {
      (_.has(result, key) ? result[key] : (result[key] = [])).push(value);
    });
  };
  _.countBy = function(obj, value, context) {
    return group(obj, value, context, function(result, key) {
      if (!_.has(result, key)) result[key] = 0;
      result[key]++;
    });
  };
  _.sortedIndex = function(array, obj, iterator, context) {
    iterator = iterator == null ? _.identity : lookupIterator(iterator);
    var value = iterator.call(context, obj);
    var low = 0, high = array.length;
    while (low < high) {
      var mid = (low + high) >>> 1;
      iterator.call(context, array[mid]) < value ? low = mid + 1 : high = mid;
    }
    return low;
  };
  _.toArray = function(obj) {
    if (!obj) return [];
    if (_.isArray(obj)) return slice.call(obj);
    if (obj.length === +obj.length) return _.map(obj, _.identity);
    return _.values(obj);
  };
  _.size = function(obj) {
    if (obj == null) return 0;
    return (obj.length === +obj.length) ? obj.length : _.keys(obj).length;
  };
  _.first = _.head = _.take = function(array, n, guard) {
    if (array == null) return void 0;
    return (n != null) && !guard ? slice.call(array, 0, n) : array[0];
  };
  _.initial = function(array, n, guard) {
    return slice.call(array, 0, array.length - ((n == null) || guard ? 1 : n));
  };
  _.last = function(array, n, guard) {
    if (array == null) return void 0;
    if ((n != null) && !guard) {
      return slice.call(array, Math.max(array.length - n, 0));
    } else {
      return array[array.length - 1];
    }
  };
  _.rest = _.tail = _.drop = function(array, n, guard) {
    return slice.call(array, (n == null) || guard ? 1 : n);
  };
  _.compact = function(array) {
    return _.filter(array, _.identity);
  };
  var flatten = function(input, shallow, output) {
    each(input, function(value) {
      if (_.isArray(value)) {
        shallow ? push.apply(output, value) : flatten(value, shallow, output);
      } else {
        output.push(value);
      }
    });
    return output;
  };
  _.flatten = function(array, shallow) {
    return flatten(array, shallow, []);
  };
  _.without = function(array) {
    return _.difference(array, slice.call(arguments, 1));
  };
  _.uniq = _.unique = function(array, isSorted, iterator, context) {
    if (_.isFunction(isSorted)) {
      context = iterator;
      iterator = isSorted;
      isSorted = false;
    }
    var initial = iterator ? _.map(array, iterator, context) : array;
    var results = [];
    var seen = [];
    each(initial, function(value, index) {
      if (isSorted ? (!index || seen[seen.length - 1] !== value) : !_.contains(seen, value)) {
        seen.push(value);
        results.push(array[index]);
      }
    });
    return results;
  };
  _.union = function() {
    return _.uniq(concat.apply(ArrayProto, arguments));
  };
  _.intersection = function(array) {
    var rest = slice.call(arguments, 1);
    return _.filter(_.uniq(array), function(item) {
      return _.every(rest, function(other) {
        return _.indexOf(other, item) >= 0;
      });
    });
  };
  _.difference = function(array) {
    var rest = concat.apply(ArrayProto, slice.call(arguments, 1));
    return _.filter(array, function(value){ return !_.contains(rest, value); });
  };
  _.zip = function() {
    var args = slice.call(arguments);
    var length = _.max(_.pluck(args, 'length'));
    var results = new Array(length);
    for (var i = 0; i < length; i++) {
      results[i] = _.pluck(args, "" + i);
    }
    return results;
  };
  _.object = function(list, values) {
    if (list == null) return {};
    var result = {};
    for (var i = 0, l = list.length; i < l; i++) {
      if (values) {
        result[list[i]] = values[i];
      } else {
        result[list[i][0]] = list[i][1];
      }
    }
    return result;
  };
  _.indexOf = function(array, item, isSorted) {
    if (array == null) return -1;
    var i = 0, l = array.length;
    if (isSorted) {
      if (typeof isSorted == 'number') {
        i = (isSorted < 0 ? Math.max(0, l + isSorted) : isSorted);
      } else {
        i = _.sortedIndex(array, item);
        return array[i] === item ? i : -1;
      }
    }
    if (nativeIndexOf && array.indexOf === nativeIndexOf) return array.indexOf(item, isSorted);
    for (; i < l; i++) if (array[i] === item) return i;
    return -1;
  };
  _.lastIndexOf = function(array, item, from) {
    if (array == null) return -1;
    var hasIndex = from != null;
    if (nativeLastIndexOf && array.lastIndexOf === nativeLastIndexOf) {
      return hasIndex ? array.lastIndexOf(item, from) : array.lastIndexOf(item);
    }
    var i = (hasIndex ? from : array.length);
    while (i--) if (array[i] === item) return i;
    return -1;
  };
  _.range = function(start, stop, step) {
    if (arguments.length <= 1) {
      stop = start || 0;
      start = 0;
    }
    step = arguments[2] || 1;

    var len = Math.max(Math.ceil((stop - start) / step), 0);
    var idx = 0;
    var range = new Array(len);

    while(idx < len) {
      range[idx++] = start;
      start += step;
    }

    return range;
  };
  _.bind = function(func, context) {
    if (func.bind === nativeBind && nativeBind) return nativeBind.apply(func, slice.call(arguments, 1));
    var args = slice.call(arguments, 2);
    return function() {
      return func.apply(context, args.concat(slice.call(arguments)));
    };
  };
  _.partial = function(func) {
    var args = slice.call(arguments, 1);
    return function() {
      return func.apply(this, args.concat(slice.call(arguments)));
    };
  };
  _.bindAll = function(obj) {
    var funcs = slice.call(arguments, 1);
    if (funcs.length === 0) funcs = _.functions(obj);
    each(funcs, function(f) { obj[f] = _.bind(obj[f], obj); });
    return obj;
  };
  _.memoize = function(func, hasher) {
    var memo = {};
    hasher || (hasher = _.identity);
    return function() {
      var key = hasher.apply(this, arguments);
      return _.has(memo, key) ? memo[key] : (memo[key] = func.apply(this, arguments));
    };
  };
  _.delay = function(func, wait) {
    var args = slice.call(arguments, 2);
    return setTimeout(function(){ return func.apply(null, args); }, wait);
  };
  _.defer = function(func) {
    return _.delay.apply(_, [func, 1].concat(slice.call(arguments, 1)));
  };
  _.throttle = function(func, wait) {
    var context, args, timeout, result;
    var previous = 0;
    var later = function() {
      previous = new Date;
      timeout = null;
      result = func.apply(context, args);
    };
    return function() {
      var now = new Date;
      var remaining = wait - (now - previous);
      context = this;
      args = arguments;
      if (remaining <= 0) {
        clearTimeout(timeout);
        timeout = null;
        previous = now;
        result = func.apply(context, args);
      } else if (!timeout) {
        timeout = setTimeout(later, remaining);
      }
      return result;
    };
  };
  _.debounce = function(func, wait, immediate) {
    var timeout, result;
    return function() {
      var context = this, args = arguments;
      var later = function() {
        timeout = null;
        if (!immediate) result = func.apply(context, args);
      };
      var callNow = immediate && !timeout;
      clearTimeout(timeout);
      timeout = setTimeout(later, wait);
      if (callNow) result = func.apply(context, args);
      return result;
    };
  };
  _.once = function(func) {
    var ran = false, memo;
    return function() {
      if (ran) return memo;
      ran = true;
      memo = func.apply(this, arguments);
      func = null;
      return memo;
    };
  };
  _.wrap = function(func, wrapper) {
    return function() {
      var args = [func];
      push.apply(args, arguments);
      return wrapper.apply(this, args);
    };
  };
  _.compose = function() {
    var funcs = arguments;
    return function() {
      var args = arguments;
      for (var i = funcs.length - 1; i >= 0; i--) {
        args = [funcs[i].apply(this, args)];
      }
      return args[0];
    };
  };
  _.after = function(times, func) {
    if (times <= 0) return func();
    return function() {
      if (--times < 1) {
        return func.apply(this, arguments);
      }
    };
  };
  _.keys = nativeKeys || function(obj) {
    if (obj !== Object(obj)) throw new TypeError('Invalid object');
    var keys = [];
    for (var key in obj) if (_.has(obj, key)) keys[keys.length] = key;
    return keys;
  };
  _.values = function(obj) {
    var values = [];
    for (var key in obj) if (_.has(obj, key)) values.push(obj[key]);
    return values;
  };
  _.pairs = function(obj) {
    var pairs = [];
    for (var key in obj) if (_.has(obj, key)) pairs.push([key, obj[key]]);
    return pairs;
  };
  _.invert = function(obj) {
    var result = {};
    for (var key in obj) if (_.has(obj, key)) result[obj[key]] = key;
    return result;
  };
  _.functions = _.methods = function(obj) {
    var names = [];
    for (var key in obj) {
      if (_.isFunction(obj[key])) names.push(key);
    }
    return names.sort();
  };
  _.extend = function(obj) {
    each(slice.call(arguments, 1), function(source) {
      if (source) {
        for (var prop in source) {
          obj[prop] = source[prop];
        }
      }
    });
    return obj;
  };
  _.pick = function(obj) {
    var copy = {};
    var keys = concat.apply(ArrayProto, slice.call(arguments, 1));
    each(keys, function(key) {
      if (key in obj) copy[key] = obj[key];
    });
    return copy;
  };
  _.omit = function(obj) {
    var copy = {};
    var keys = concat.apply(ArrayProto, slice.call(arguments, 1));
    for (var key in obj) {
      if (!_.contains(keys, key)) copy[key] = obj[key];
    }
    return copy;
  };
  _.defaults = function(obj) {
    each(slice.call(arguments, 1), function(source) {
      if (source) {
        for (var prop in source) {
          if (obj[prop] == null) obj[prop] = source[prop];
        }
      }
    });
    return obj;
  };
  _.clone = function(obj) {
    if (!_.isObject(obj)) return obj;
    return _.isArray(obj) ? obj.slice() : _.extend({}, obj);
  };
  _.tap = function(obj, interceptor) {
    interceptor(obj);
    return obj;
  };
  var eq = function(a, b, aStack, bStack) {
    if (a === b) return a !== 0 || 1 / a == 1 / b;
    if (a == null || b == null) return a === b;
    if (a instanceof _) a = a._wrapped;
    if (b instanceof _) b = b._wrapped;
    var className = toString.call(a);
    if (className != toString.call(b)) return false;
    switch (className) {
      case '[object String]':
        return a == String(b);
      case '[object Number]':
        return a != +a ? b != +b : (a == 0 ? 1 / a == 1 / b : a == +b);
      case '[object Date]':
      case '[object Boolean]':
        return +a == +b;
      case '[object RegExp]':
        return a.source == b.source &&
               a.global == b.global &&
               a.multiline == b.multiline &&
               a.ignoreCase == b.ignoreCase;
    }
    if (typeof a != 'object' || typeof b != 'object') return false;
    var length = aStack.length;
    while (length--) {
      if (aStack[length] == a) return bStack[length] == b;
    }
    aStack.push(a);
    bStack.push(b);
    var size = 0, result = true;
    if (className == '[object Array]') {
      size = a.length;
      result = size == b.length;
      if (result) {
        while (size--) {
          if (!(result = eq(a[size], b[size], aStack, bStack))) break;
        }
      }
    } else {
      var aCtor = a.constructor, bCtor = b.constructor;
      if (aCtor !== bCtor && !(_.isFunction(aCtor) && (aCtor instanceof aCtor) &&
                               _.isFunction(bCtor) && (bCtor instanceof bCtor))) {
        return false;
      }
      for (var key in a) {
        if (_.has(a, key)) {
          size++;
          if (!(result = _.has(b, key) && eq(a[key], b[key], aStack, bStack))) break;
        }
      }
      if (result) {
        for (key in b) {
          if (_.has(b, key) && !(size--)) break;
        }
        result = !size;
      }
    }
    aStack.pop();
    bStack.pop();
    return result;
  };
  _.isEqual = function(a, b) {
    return eq(a, b, [], []);
  };
  _.isEmpty = function(obj) {
    if (obj == null) return true;
    if (_.isArray(obj) || _.isString(obj)) return obj.length === 0;
    for (var key in obj) if (_.has(obj, key)) return false;
    return true;
  };
  _.isElement = function(obj) {
    return !!(obj && obj.nodeType === 1);
  };
  _.isArray = nativeIsArray || function(obj) {
    return toString.call(obj) == '[object Array]';
  };
  _.isObject = function(obj) {
    return obj === Object(obj);
  };
  each(['Arguments', 'Function', 'String', 'Number', 'Date', 'RegExp'], function(name) {
    _['is' + name] = function(obj) {
      return toString.call(obj) == '[object ' + name + ']';
    };
  });
  if (!_.isArguments(arguments)) {
    _.isArguments = function(obj) {
      return !!(obj && _.has(obj, 'callee'));
    };
  }
  if (typeof (/./) !== 'function') {
    _.isFunction = function(obj) {
      return typeof obj === 'function';
    };
  }
  _.isFinite = function(obj) {
    return isFinite(obj) && !isNaN(parseFloat(obj));
  };
  _.isNaN = function(obj) {
    return _.isNumber(obj) && obj != +obj;
  };
  _.isBoolean = function(obj) {
    return obj === true || obj === false || toString.call(obj) == '[object Boolean]';
  };
  _.isNull = function(obj) {
    return obj === null;
  };
  _.isUndefined = function(obj) {
    return obj === void 0;
  };
  _.has = function(obj, key) {
    return hasOwnProperty.call(obj, key);
  };
  _.noConflict = function() {
    root._ = previousUnderscore;
    return this;
  };
  _.identity = function(value) {
    return value;
  };
  _.times = function(n, iterator, context) {
    var accum = Array(n);
    for (var i = 0; i < n; i++) accum[i] = iterator.call(context, i);
    return accum;
  };
  _.random = function(min, max) {
    if (max == null) {
      max = min;
      min = 0;
    }
    return min + Math.floor(Math.random() * (max - min + 1));
  };
  var entityMap = {
    escape: {
      '&': '&amp;',
      '<': '&lt;',
      '>': '&gt;',
      '"': '&quot;',
      "'": '&#x27;',
      '/': '&#x2F;'
    }
  };
  entityMap.unescape = _.invert(entityMap.escape);
  var entityRegexes = {
    escape:   new RegExp('[' + _.keys(entityMap.escape).join('') + ']', 'g'),
    unescape: new RegExp('(' + _.keys(entityMap.unescape).join('|') + ')', 'g')
  };
  _.each(['escape', 'unescape'], function(method) {
    _[method] = function(string) {
      if (string == null) return '';
      return ('' + string).replace(entityRegexes[method], function(match) {
        return entityMap[method][match];
      });
    };
  });
  _.result = function(object, property) {
    if (object == null) return null;
    var value = object[property];
    return _.isFunction(value) ? value.call(object) : value;
  };
  _.mixin = function(obj) {
    each(_.functions(obj), function(name){
      var func = _[name] = obj[name];
      _.prototype[name] = function() {
        var args = [this._wrapped];
        push.apply(args, arguments);
        return result.call(this, func.apply(_, args));
      };
    });
  };
  var idCounter = 0;
  _.uniqueId = function(prefix) {
    var id = ++idCounter + '';
    return prefix ? prefix + id : id;
  };
  _.templateSettings = {
    evaluate    : /<%([\s\S]+?)%>/g,
    interpolate : /<%=([\s\S]+?)%>/g,
    escape      : /<%-([\s\S]+?)%>/g
  };
  var noMatch = /(.)^/;
  var escapes = {
    "'":      "'",
    '\\':     '\\',
    '\r':     'r',
    '\n':     'n',
    '\t':     't',
    '\u2028': 'u2028',
    '\u2029': 'u2029'
  };

  var escaper = /\\|'|\r|\n|\t|\u2028|\u2029/g;
  _.template = function(text, data, settings) {
    var render;
    settings = _.defaults({}, settings, _.templateSettings);
    var matcher = new RegExp([
      (settings.escape || noMatch).source,
      (settings.interpolate || noMatch).source,
      (settings.evaluate || noMatch).source
    ].join('|') + '|$', 'g');
    var index = 0;
    var source = "__p+='";
    text.replace(matcher, function(match, escape, interpolate, evaluate, offset) {
      source += text.slice(index, offset)
        .replace(escaper, function(match) { return '\\' + escapes[match]; });

      if (escape) {
        source += "'+\n((__t=(" + escape + "))==null?'':_.escape(__t))+\n'";
      }
      if (interpolate) {
        source += "'+\n((__t=(" + interpolate + "))==null?'':__t)+\n'";
      }
      if (evaluate) {
        source += "';\n" + evaluate + "\n__p+='";
      }
      index = offset + match.length;
      return match;
    });
    source += "';\n";
    if (!settings.variable) source = 'with(obj||{}){\n' + source + '}\n';

    source = "var __t,__p='',__j=Array.prototype.join," +
      "print=function(){__p+=__j.call(arguments,'');};\n" +
      source + "return __p;\n";

    try {
      render = new Function(settings.variable || 'obj', '_', source);
    } catch (e) {
      e.source = source;
      throw e;
    }

    if (data) return render(data, _);
    var template = function(data) {
      return render.call(this, data, _);
    };
    template.source = 'function(' + (settings.variable || 'obj') + '){\n' + source + '}';

    return template;
  };
  _.chain = function(obj) {
    return _(obj).chain();
  };
  var result = function(obj) {
    return this._chain ? _(obj).chain() : obj;
  };
  _.mixin(_);
  each(['pop', 'push', 'reverse', 'shift', 'sort', 'splice', 'unshift'], function(name) {
    var method = ArrayProto[name];
    _.prototype[name] = function() {
      var obj = this._wrapped;
      method.apply(obj, arguments);
      if ((name == 'shift' || name == 'splice') && obj.length === 0) delete obj[0];
      return result.call(this, obj);
    };
  });
  each(['concat', 'join', 'slice'], function(name) {
    var method = ArrayProto[name];
    _.prototype[name] = function() {
      return result.call(this, method.apply(this._wrapped, arguments));
    };
  });

  _.extend(_.prototype, {
    chain: function() {
      this._chain = true;
      return this;
    },
    value: function() {
      return this._wrapped;
    }

  });

}).call(this);

})()
},
{}]
},{},["E/GbHF"])
;

function req() {return require.apply(this, arguments)}
module.exports = req("jshint");

});