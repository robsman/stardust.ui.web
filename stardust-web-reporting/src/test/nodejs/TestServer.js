var http = require('http');

http
		.createServer(
				function(request, response) {
					console.log("Origin: " + request.headers.origin);

					// var origin = (request.headers.origin || "*");
					var origin = "*";

					console.log("Origin: " + origin);

					if (request.method.toUpperCase() === "OPTIONS") {
						console.log("OPTIONS requested.");

						response
								.writeHead(
										"204",
										"No Content",
										{
											"access-control-allow-origin" : origin,
											"access-control-allow-methods" : "GET, POST, PUT, DELETE, OPTIONS",
											"access-control-allow-headers" : "Origin, X-Requested-With, Content-Type, Accept",
											"access-control-max-age" : 10, // Seconds.
											"content-length" : 0
										});

						return (response.end());
					}

					var requestBodyBuffer = [];

					request.on("data", function(chunk) {
						console.log("push:" + chunk);
						requestBodyBuffer.push(chunk);
					});

					request
							.on(
									"end",
									function() {
										var list = [];

										for ( var n = 0; n < 100; ++n) {
											var record = {};
											record.firstName = "Hanna";
											record.lastName = "LastName" + n;
											record.known = true;
											record.numberOfDependents = n;

											list.push(record);
										}

										var returnObject = {
											list : list
										};

										var returnString = JSON
												.stringify(returnObject);

										console.log("Write head");

										response
												.writeHead(
														"200",
														"OK",
														{
															"access-control-allow-origin" : origin,
															"access-control-allow-headers" : "Origin, X-Requested-With, Content-Type, Accept",
															"content-type" : "application/json",
															"content-length" : returnString.length
														});

										return (response.end(returnString));
									});

				}).listen(1337);

console.log('Server running at http://localhost:1337/');