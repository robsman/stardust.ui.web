define(
    [],
    function() {
      return {
        initialize: initialize
      };

      /**
       * 
       */
      function initialize(module) {
        module.directive('ippErrorMessages', function() {
          return {
            restrict: "E",
            require: '?ngModel',
            link: function(scope, element, attributes, controller) {
              scope.$watch(attributes.ngModel, function(value) {
                element.empty();

                var errorList = scope.$eval(attributes.ngModel);

                console.log("Error List:");
                console.log(errorList);

                if (errorList == null || errorList.length == 0) {
                  return;
                }

                var div = jQuery("<div class='errorMessagesPanel'></div>");

                element.append(div);

                var ul = jQuery("<ul></ul>");

                div.append(ul);

                for (var n = 0; n < errorList.length; ++n) {
                  ul.append("<li><span class='errorMessage'>" + errorList[n].message + "</span></li>");
                }
              });
            }
          }
        });

        /**
         * Problems:
         * 
         * A timeout had to be introduced to wait for Angular to complete DOM operations.
         */
        module
            .directive(
                'sdTableData',
                function() {
                  return {
                    restrict: "A",
                    transclude: "element",
                    compile: function(element, attrs, linker) {
                      var aoColumnDefs = [
                        {
                          sDefaultContent: "-",
                          sClass: "",
                          aTargets: [
                            "_all"
                          ]
                        }
                      ];

                      return {
                        post: function(scope, element, attributes, controller) {
                          // Parse expression

                          var expression = attrs.sdTableData;
                          var match = expression.match(/^\s*(.+)\s+in\s+(.*?)\s*(\s+track\s+by\s+(.+)\s*)?$/), trackByExp, trackByExpGetter, trackByIdExpFn, trackByIdArrayFn, trackByIdObjFn, lhs, rhs, valueIdentifier, keyIdentifier/*
                                                                                                                                                                                                                                         * ,
                                                                                                                                                                                                                                         * hashFnLocals = {
                                                                                                                                                                                                                                         * $id :
                                                                                                                                                                                                                                         * hashKey }
                                                                                                                                                                                                                                         */;

                          if (!match) {
                            throw "Expected expression in form of '_item_ in _collection_[ track by _id_]' but got '{0}'.";
                          }

                          lhs = match[1];
                          rhs = match[2];
                          trackByExp = match[4];

                          if (trackByExp) {
                            trackByExpGetter = 0/*
                                                 * scope .$parse(trackByExp)
                                                 */;
                            trackByIdExpFn = function(key, value, index) {
                              // assign key,
                              // value, and $index
                              // to the locals so
                              // that they can be
                              // used in hash
                              // functions
                              if (keyIdentifier) hashFnLocals[keyIdentifier] = key;
                              hashFnLocals[valueIdentifier] = value;
                              hashFnLocals.$index = index;
                              return trackByExpGetter($scope, hashFnLocals);
                            };
                          } else {
                            trackByIdArrayFn = function(key, value) {
                              return hashKey(value);
                            };
                            trackByIdObjFn = function(key) {
                              return key;
                            };
                          }

                          match = lhs.match(/^(?:([\$\w]+)|\(([\$\w]+)\s*,\s*([\$\w]+)\))$/);
                          if (!match) {
                            throw ngRepeatMinErr(
                                'iidexp',
                                "'_item_' in '_item_ in _collection_' should be an identifier or '(_key_, _value_)' expression, but got '{0}'.",
                                lhs);
                          }
                          valueIdentifier = match[3] || match[1];
                          keyIdentifier = match[2];

                          var elements = [];
                          var parent = element.parent();
                          var table = jQuery(parent.parent());

                          scope.$watch(rhs, function(value) {
                            if (value == null || value.length == 0) {
                              return;
                            }

                            if (table.fnDestroy != null) {
                              table.fnDestroy();
                              console.log("Destroyed");
                            }

                            var i, block, childScope;

                            // check
                            // if
                            // elements
                            // have
                            // already
                            // been
                            // rendered

                            if (elements.length > 0) {
                              // if
                              // so
                              // remove
                              // them
                              // from
                              // DOM,
                              // and
                              // destroy
                              // their
                              // scope
                              for (i = 0; i < elements.length; i++) {
                                elements[i].el.remove();
                                elements[i].scope.$destroy();
                              }

                              elements = [];
                            }

                            for (n = 0; n < value.length; ++n) {
                              var rowScope = scope.$new();

                              rowScope[lhs] = value[n];
                              rowScope.$index = n;
                              rowScope.$first = (n === 0);
                              rowScope.$last = (n === (value.length - 1));
                              rowScope.$middle = !(rowScope.$first || rowScope.$last);
                              rowScope.$odd = !(rowScope.$even = (n & 1) === 0);

                              linker(rowScope, function(clone) {
                                parent.append(clone); // Add
                                // to
                                // DOM
                                jQuery(clone).prop("id", "sdTableRowIndex" + n);

                                block = {};
                                block.el = clone;
                                block.scope = rowScope;
                                elements.push(block);
                              });
                            }

                            document.body.style.cursor = "wait";

                            // There
                            // might
                            // be a
                            // way
                            // to
                            // synchronize
                            // against
                            // Angular
                            // JS
                            // operations;
                            // using
                            // timeout
                            // meanwhile

                            window.setTimeout(function() {
                              if (attributes.sdTableSelection) {
                                // Clear
                                // selection

                                scope.$eval(attributes.sdTableSelection).length = 0;
                                table.find("tbody tr").removeClass("selectedRow");

                                // Unbind
                                // events

                                table.find("tbody tr").unbind("click");

                                // Bind
                                // click
                                // events

                                table.find("tbody tr").click(
                                    function(event) {
                                      var selection = scope.$eval(attributes.sdTableSelection);
                                      var indexString = jQuery(this).prop("id");

                                      indexString = indexString.substring(indexString.indexOf("sdTableRowIndex") +
                                          "sdTableRowIndex".length);

                                      var index = parseInt(indexString);

                                      if (event.ctrlKey) {
                                        var indexInSelection;

                                        if ((indexInSelection = jQuery.inArray(value[index], selection)) > -1) {
                                          selection.splice(indexInSelection, 1);
                                        } else {
                                          selection.push(value[index]);
                                        }

                                        jQuery(this).toggleClass("selectedRow");
                                      } else {
                                        table.find("tbody tr").removeClass("selectedRow");
                                        jQuery(this).addClass("selectedRow");

                                        selection.length = 0;
                                        selection.push(value[index]);
                                      }

                                      scope.$apply();
                                    });
                              }

                              // Mark
                              // first
                              // row

                              table.find("tbody tr").last().addClass("lastRow");

                              // Create
                              // Datatables

                              try {
                                table.dataTable({
                                  aoColumnDefs: aoColumnDefs
                                });
                              } catch (x) {
                                console.log("Cannot create data table");
                                console.log(x);
                              }

                              document.body.style.cursor = "default";
                            }, 1000);
                          });

                          scope.$watch(attributes.sdTableSelection, function(value) {
                            var rowObjects = scope.$eval(rhs);

                            if (rowObjects) {

                              table.find("tbody tr").removeClass("selectedRow");

                              for (var n = 0; n < rowObjects.length; ++n) {
                                for (var m = 0; m < value.length; ++m) {
                                  if (rowObjects[n] == value[m]) {
                                    table.find("#sdTableRowIndex" + n).addClass("selectedRow");
                                  }
                                }
                              }
                            }
                          });
                        }
                      };
                    }
                  };
                });

        module
            .directive('sdDialog',
                function() {
                  return {
                    restrict: "A",
                    compile: function(element, attrs) {
                      return {
                        post: function(scope, element, attributes, controller) {
                          // Delayed to allow Angular to finish

                          window
                              .setTimeout(
                                  function() {
                                    dialog = jQuery(element).dialog({
                                      autoOpen: false,
                                      resize: "auto",
                                      width: 800,
                                      resizeable: false
                                    });

                                    dialog.css("visibility", "visible");

                                    // TODO Requires format
                                    // segment1.segment2 ... for
                                    // the dialog field; not
                                    // robust

                                    if (attributes.sdDialog.lastIndexOf(".") < 0) {
                                      scope[attributes.ippDialog] = dialog;

                                      console.log("Scope");
                                      console.log(scope[attributes.sdDialog]);
                                    } else {
                                      scope.$eval(attributes.sdDialog.substring(0, attributes.ippDialog
                                          .lastIndexOf(".")))[attributes.sdDialog.substring(attributes.ippDialog
                                          .lastIndexOf(".") + 1)] = dialog;
                                    }
                                  }, 1000);
                        }
                      };
                    }
                  };
                });

        module.directive('ippCalendar',
            function() {
              return {
                restrict: "A",
                compile: function(element, attrs) {
                  return {
                    post: function(scope, element, attributes, controller) {

                      scope[attributes.sdDialog] = jQuery(element).fullCalendar(
                          scope.$eval(attributes.ippCalendarOptions));
                      scope[attributes.sdDialog].fullCalendar('today');
                    }
                  };
                }
              };
            });

        // Until Agular has it

        module.directive('ngBlur', function() {
          return function(scope, elem, attrs) {
            elem.bind('blur', function() {
              scope.$apply(attrs.ngBlur);
            });
          };
        });
      }
    });