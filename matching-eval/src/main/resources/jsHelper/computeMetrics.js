            //compute aggregated values/metrics like precision recall fmeasure:
            function compute_metrics_for_group(source_group) {
                return {
                    all:function () {
                    console.log('compute');
                        var data = {
                            all_tp: 0, all_fp: 0, all_fn: 0,number_testcases: 0,
                            macro: {name: 'macro', precision: 0, recall: 0, f1: 0},
                            micro: {name: 'micro', precision: 0, recall: 0, f1: 0}
                        };
                        source_group.all().map(function(d) {
                            var tp = d.value["true positive"] || 0;
                            var fp = d.value["false positive"] || 0;
                            var fn = d.value["false negative"] || 0;
                            data.all_tp += tp;
                            data.all_fp += fp;
                            data.all_fn += fn;
                            data.macro.precision += safe_division(tp, tp + fp);
                            data.macro.recall += safe_division(tp, tp + fn);
                            if(tp+fp+fn !== 0)
                                data.number_testcases += 1;
                        });
                        data.micro.precision = safe_division(data.all_tp, data.all_tp + data.all_fp);
                        data.micro.recall = safe_division(data.all_tp, data.all_tp + data.all_fn);
                        data.micro.f1 = safe_division(2 * data.micro.precision * data.micro.recall, data.micro.precision + data.micro.recall);

                        data.macro.precision  = safe_division(data.macro.precision,data.number_testcases);
                        data.macro.recall     = safe_division(data.macro.recall,data.number_testcases);
                        data.macro.f1         = safe_division(2 * data.macro.precision * data.macro.recall, data.macro.precision + data.macro.recall); 
                        
                        //toFixed
                        ["precision", "recall", "f1"].forEach(function(key){ data.micro[key] = data.micro[key].toFixed(4) });
                        ["precision", "recall", "f1"].forEach(function(key){ data.macro[key] = data.macro[key].toFixed(4) });

                        //return [ {key:'micro', value:data.micro}, {key:'macro', value:data.macro} ];
                        return [ data.micro, data.macro ];
                    },
                    top:function(n) {
                        return this.all();
                    },
                    bottom:function(n) {
                        return this.all();
                    }
                };
            }
            function safe_division(numerator, denominator){
                if (!denominator) {// Matches +0, -0, NaN or use: if(denominator === 0 || isNaN(denominator))
                    return 0;
                }
                else {
                    return numerator / denominator;
                }
            }