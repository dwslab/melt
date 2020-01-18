            //reducer functions for crossfilter
            function reduceSortedAttributeAdd(attr, evalResultAttr) {
              return function(p,v) {
                // keep array sorted for efficiency
                if(v[evalResultAttr] !== "false negative"){
                    p.splice(d3.bisectLeft(p, +v[attr]), 0, +v[attr]);
                }
                return p;
              };
            }
            function reduceSortedAttributeRemove(attr) {
              return function(p,v) {
                p.splice(d3.bisectLeft(p, +v[attr]), 1);
                return p;
              };
            }
            function reduceSortedAttributeInit() {
              return function(){return [];};
            }