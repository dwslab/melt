            //reducer functions for crossfilter
            function reduceTwoFielsdAdd(attrOne, attrTwo) {
              return function(p,v) {
                var groupOne = p[v[attrOne]];
                if(!groupOne){
                    groupOne = {};
                    p[v[attrOne]] = groupOne;
                }
                groupOne[v[attrTwo]] = (groupOne[v[attrTwo]] || 0) + 1;
                return p;
              };
            }
            function reduceTwoFieldsRemove(attrOne, attrTwo) {
              return function(p,v) {
                var groupOne = p[v[attrOne]];
                if(!groupOne){
                    groupOne = {};
                    p[v[attrOne]] = groupOne;
                }
                groupOne[v[attrTwo]] = (groupOne[v[attrTwo]] || 0) - 1;
                return p;
              };
            }
            function reduceTwoFieldsInit() {
              return function(){return {};};
            }