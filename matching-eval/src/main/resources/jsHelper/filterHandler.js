            //special filter handler for confidence
            function confidenceFilterHandler(dimension, filters){
                if (filters.length === 0) {
                    dimension.filter(null);
                } else if (filters.length === 1 && !filters[0].isFiltered) {
                    // single value and not a function-based filter
                    dimension.filterExact(filters[0]);
                } else if (filters.length === 1 && filters[0].filterType === 'RangedFilter') {
                    // single range-based filter
                    //dimension.filterRange(filters[0]);
                    var min = filters[0][0];
                    var max = filters[0][1];
                    dimension.filterFunction(function(d) {
                        var split = d.split(':');
                        if(split[1] === "false negative") {return true;}
                        var confidence = +split[0];
                        var test = (min < confidence) && (confidence < max);
                        return test;
                    });
                } else {
                    dimension.filterFunction(function (d) {
                        for (var i = 0; i < filters.length; i++) {
                            var filter = filters[i];
                            if (filter.isFiltered && filter.isFiltered(d)) {
                                return true;
                            } else if (filter <= d && filter >= d) {
                                return true;
                            }
                        }
                        return false;
                    });
                }
                return filters;
            }