            //reducer functions for crossfilter
            function reduceFieldAdd(attr) {
              return function(p,v) {
                p[v[attr]] = (p[v[attr]] || 0) + 1;
                return p;
              };
            }
            function reduceFieldRemove(attr) {
              return function(p,v) {
                p[v[attr]] = (p[v[attr]] || 0) - 1;//--p[v[attr]] || 0;
                return p;
              };
            }
            function reduceFieldInit() {
              return function(){return {};};
            }