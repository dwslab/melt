            //reducer functions for crossfilter
            function reduceAddGroup(attr) {
              return function(p,v) {
                p[v[attr]] = (p[v[attr]] || 0) + 1;
                return p;
              };
            }
            function reduceRemoveGroup(attr) {
              return function(p,v) {
                p[v[attr]] = (p[v[attr]] || 0) - 1;//--p[v[attr]] || 0;
                return p;
              };
            }
            function reduceInitGroup() {
              return {};
            }