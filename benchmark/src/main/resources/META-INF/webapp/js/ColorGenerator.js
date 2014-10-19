define(
		[],
		function() {
			return {
				create : function(parameters) {
					var colorGenerator = new ColorGenerator();

					colorGenerator.initialize(parameters);

					return colorGenerator;
				}
			};

			function ColorGenerator() {
				ColorGenerator.prototype.initialize = function(a) {
					this.resultList = [ this ];
					this.elm = null;

					if (typeof (a) == "undefined") {
						a = "#000000";
					}

					// if (typeof (a.validateHsl) == "function") {
					this.setHsl(a);
					// } else {
					// this.setColor(a);
					// }
				};

				ColorGenerator.prototype.setColor = function(a) {
					if (typeof (a) == "undefined") {
						return

					}

					if (typeof (a) == "object") {
						this.setHsl(a)
					} else {
						this.setHex(a)
					}
				};

				ColorGenerator.prototype.setHsl = function(a) {
					this.h = a[0];
					this.s = a[1];
					this.l = a[2];
					this.validateHsl();
					return this
				};
				
				ColorGenerator.prototype.validateHsl = function() {
					this.h = this.h % 360;
					if (this.h < 0) {
						this.h += 360
					}
					if (this.s < 0) {
						this.s = 0
					}
					if (this.s > 100) {
						this.s = 100
					}
					if (this.l < 0) {
						this.l = 0
					}
					if (this.l > 100) {
						this.l = 100
					}
				};
				
				ColorGenerator.prototype.setHex = function(e) {
					if (e.substring(0, 1) == "#") {
						e = e.substring(1)
					}
					var d = parseInt(e.substring(0, 2), 16);
					var c = parseInt(e.substring(2, 4), 16);
					var a = parseInt(e.substring(4, 6), 16);
					this.setRgb([ d, c, a ]);
					return this
				};
				
				ColorGenerator.prototype.setRgb = function(e) {
					var i = e[0] / 255;
					var h = e[1] / 255;
					var c = e[2] / 255;
					var a = Math.max(i, h, c);
					var f = Math.min(i, h, c);
					this.h = (a + f) / 2;
					this.s = this.h;
					this.l = this.h;
					if (a == f) {
						this.h = 0;
						this.s = 0
					} else {
						var j = a - f;
						this.s = this.l > 0.5 ? j / (2 - a - f) : j / (a + f);
						switch (a) {
						case i:
							this.h = (h - c) / j + (h < c ? 6 : 0);
							break;
						case h:
							this.h = (c - i) / j + 2;
							break;
						case c:
							this.h = (i - h) / j + 4;
							break
						}
						this.h = this.h / 6
					}
					this.h = 360 * this.h;
					this.s = 100 * this.s;
					this.l = 100 * this.l;
					return this
				};
				
				ColorGenerator.prototype.hue2rgb = function(c, b, a) {
					if (a < 0) {
						a += 1
					}
					if (a > 1) {
						a -= 1
					}
					if (a < 1 / 6) {
						return c + (b - c) * 6 * a
					}
					if (a < 1 / 2) {
						return b
					}
					if (a < 2 / 3) {
						return c + (b - c) * (2 / 3 - a) * 6
					}
					return c
				};
				
				ColorGenerator.prototype.getRgb = function() {
					this.validateHsl();
					var e = this.h / 360;
					var d = this.s / 100;
					var c = this.l / 100;
					var i = c;
					var f = c;
					var a = c;
					if (d != 0) {
						var j = c < 0.5 ? c * (1 + d) : c + d - c * d;
						var k = 2 * c - j;
						i = this.hue2rgb(k, j, e + 1 / 3);
						f = this.hue2rgb(k, j, e);
						a = this.hue2rgb(k, j, e - 1 / 3)
					}
					return [ Math.round(i * 255), Math.round(f * 255),
							Math.round(a * 255) ]
				};
				
				ColorGenerator.prototype.getHex = function() {
					var a = this.getRgb();
					var b = this.toHexByte(a[0]);
					b += this.toHexByte(a[1]);
					b += this.toHexByte(a[2]);
					return "#" + b.toUpperCase()
				};
				
				ColorGenerator.prototype.toHexByte = function(b) {
					var a = b.toString(16);
					if (a.length < 2) {
						a = "0" + a
					}
					return a
				};
				
				ColorGenerator.prototype.getHsl = function() {
					this.validateHsl();
					return [ this.h, this.s, this.l ]
				};
				
				ColorGenerator.prototype.multi = function(j, o, n, m, l, k, h,
						f, d, c) {
					var e = [].concat(this.resultList);
					this.resultList = [];
					for ( var b in e) {
						var a = e[b];
						a.workList = [];
						if (j == "rel") {
							ColorGenerator.prototype.spinSingle.call(a, "rel",
									o, n, m, l, k, h, f, d, c)
						}
						if (j == "abs") {
							ColorGenerator.prototype.spinSingle.call(a, "abs",
									o, n, m, l, k, h, f, d, c)
						}
						this.resultList = this.resultList.concat(a.workList)
					}
					if (this.resultList.length == 0) {
						return this
					}
					var g = this.resultList[this.resultList.length - 1];
					this.h = g.h;
					this.s = g.s;
					this.l = g.l;
					return this
				};
				
				ColorGenerator.prototype.rel = function(d, c, a, b, e) {
					return this.multi("rel", d, c, a, b, e)
				};
				
				ColorGenerator.prototype.abs = function(d, c, a, b, g) {
					var f = false;
					if (typeof (d) == "object") {
						if (typeof (d.validateHsl) == "function") {
							f = true
						}
					} else {
						if (("" + d).substring(0, 1) == "#") {
							f = true
						}
						if (("" + d).length > 4) {
							f = true
						}
					}
					if (f) {
						var e = new ColorGenerator(d);
						return this.multi("abs", e.h, e.s, e.l, c, a)
					} else {
						return this.multi("abs", d, c, a, b, g)
					}
				};
				
				ColorGenerator.prototype.spinSingle = function(i, l, f, j, d, c) {
					var h = (i == "abs" ? -1 : 0);
					if (typeof (l) == "undefined") {
						l = h
					}
					if (typeof (f) == "undefined") {
						f = h
					}
					if (typeof (j) == "undefined") {
						j = h
					}
					if (typeof (l) == "undefined") {
						d = 12
					}
					var o = 0;
					var k = 0;
					var n = 0;
					if (typeof (l) == "object") {
						o = l.length
					}
					if (typeof (f) == "object") {
						k = f.length
					}
					if (typeof (j) == "object") {
						n = j.length
					}
					if (typeof (d) == "undefined") {
						d = 1;
						if (o > d) {
							d = o
						}
						if (k > d) {
							d = k
						}
						if (n > d) {
							d = n
						}
					}
					if (typeof (c) == "undefined") {
						c = 0
					}
					var e = null;
					if (typeof (d) == "object") {
						e = d;
						d = e.length
					}
					for (step = c; step < d; step++) {
						var p = new ColorGenerator(this);
						var a = (d == 1 ? 1 : step / (d - 1));
						var g;
						var m;
						var b;
						if (o > 0) {
							g = l[step % o]
						} else {
							g = l * a
						}
						if (k > 0) {
							m = f[step % k]
						} else {
							m = f * a
						}
						if (n > 0) {
							b = j[step % n]
						} else {
							b = j * a
						}
						if (i == "rel") {
							p.h += g;
							p.s += m;
							p.l += b
						} else {
							if (l == h) {
								p.h = this.h
							} else {
								if (o == 0) {
									p.h = this.calcLinearGradientStep(step, d,
											this.h, l)
								} else {
									p.h = g
								}
							}
							if (f == h) {
								p.s = this.s
							} else {
								if (k == 0) {
									p.s = this.calcLinearGradientStep(step, d,
											this.s, f)
								} else {
									p.s = m
								}
							}
							if (j == h) {
								p.l = this.l
							} else {
								if (n == 0) {
									p.l = this.calcLinearGradientStep(step, d,
											this.l, j)
								} else {
									p.l = b
								}
							}
						}
						p.step = step;
						if (e) {
							p.elm = e.eq(step)
						}
						this.workList[step] = p
					}
				};
				
				ColorGenerator.prototype.calcLinearGradientStep = function(d,
						c, e, f) {
					var b = (d / (c - 1));
					var a = e + ((f - e) * b);
					return a
				};
				
				ColorGenerator.prototype.each = function(b) {
					for ( var a in this.resultList) {
						b.call(this.resultList[a], this.resultList[a].elm)
					}
				};
				
				ColorGenerator.prototype.get = function(a) {
					if (typeof (a) == "undefined") {
						a = 0
					}
					return this.resultList[a]
				};
				
				ColorGenerator.prototype.isDark = function() {
					return (!this.isLight())
				};
				
				ColorGenerator.prototype.isLight = function() {
					var b = this.getRgb();
					var a = (0.299 * b[0]) + (0.587 * b[1]) + (0.114 * b[2]);
					return (a > 127)
				};
			}
		});