define(function(){
  var dataFactory={
    columnConfig: [
          {type:"text"},
          {type:"numeric",readOnly:true},
          {type:"text"}
      ],
    data:[
          [0, 1, 'some text'],
          [0, 2, 'more text'],
          [0, 3, 'even more']
      ],
    colWidths:[35,30,90],
    colHeaders:["|NA|Header",
                "#|NA|Header",
                "Description|NA|Header"]
  };
  return dataFactory;
});