            //special filter handler for confidence
            function confidenceFilterHandler(dimension, filters){
                if (filters.length === 0) {
                    dimension.filter(null);
                    return;
                }
                // single range-based filter
                //dimension.filterRange(filters[0]);
                const filter = filters[0],
                    rf = dc.filters.RangedFilter(filter[0],filter[1]);
                dimension.filterFunction(function(d) {
                    var split = d.split(':');
                    if(split[1] === "false negative") {return true;}
                    var confidence = +split[0];
                    return rf.isFiltered(confidence);
                });
                return filters;
            }