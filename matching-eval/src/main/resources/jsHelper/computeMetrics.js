            //compute aggregated values/metrics like precision recall fmeasure:
            function safe_division(numerator, denominator){
                if (!denominator) {// Matches +0, -0, NaN or use: if(denominator === 0 || isNaN(denominator))
                    return 0;
                }
                return numerator / denominator;
            }
            
            function compute_f_beta_measure(betasquared, precision, recall){
                return safe_division((1 + betasquared) * (precision * recall), (betasquared * precision) + recall);
            }
            
            function compute_metrics(testCases, beta){
                var data = {
                    all_tp: 0, all_fp: 0, all_fn: 0,number_testcases: 0,
                    macro: {precision: 0, recall: 0, fmeasure: 0, 'name' : 'macro'},
                    micro: {precision: 0, recall: 0, fmeasure: 0, 'name' : 'micro'}
                };
                testCases.forEach(function(d) {
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
                var betasquared = beta * beta;
                data.micro.precision  = safe_division(data.all_tp, data.all_tp + data.all_fp);
                data.micro.recall     = safe_division(data.all_tp, data.all_tp + data.all_fn);
                data.micro.fmeasure   = compute_f_beta_measure(betasquared, data.micro.precision, data.micro.recall);

                data.macro.precision  = safe_division(data.macro.precision,data.number_testcases);
                data.macro.recall     = safe_division(data.macro.recall,data.number_testcases);
                data.macro.fmeasure   = compute_f_beta_measure(betasquared, data.macro.precision, data.macro.recall); 

                ["precision", "recall", "fmeasure"].forEach(function(key){ data.micro[key] = data.micro[key].toFixed(4) });
                ["precision", "recall", "fmeasure"].forEach(function(key){ data.macro[key] = data.macro[key].toFixed(4) });
                return data;
            }
            
            function compute_metrics_for_group(source_group) {
                return {
                    all:function () {
                        var data = compute_metrics(source_group.all(), 1.0);
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
            
            //compute metrics for each matcher
            function compute_matcher_metrics(source_group) {
                return {
                    all:function () {
                        return source_group.all().map(function(d) {
                            var result = Object.keys(d.value).map(function(key) {
                              return {'key':key, 'value':d.value[key] };
                            });
                            var data = compute_metrics(result, 1.0);
                            data.micro.name = d.key
                            return data.micro;
                        });
                    },
                    top:function(n) {
                        return this.all();
                    },
                    bottom:function(n) {
                        return this.all();
                    }
                };
            }
            
            //compute metrics for each matcher and for all selected
            function make_row_object(name, data){
                return {
                    'Name' : name, 
                    'Prec(micro)' : data.micro.precision,
                    'Rec(micro)': data.micro.recall,
                    'F-m.(micro)': data.micro.fmeasure,
                    'Prec(macro)' : data.macro.precision,
                    'Rec(macro)': data.macro.recall,
                    'F-m.(macro)': data.macro.fmeasure,
                };
            }
            function compute_matcher_selected_metrics(group_testcase_result, group_matcher_testcase_result) {
                return {
                    all:function () {
                        var results = group_matcher_testcase_result.all().map(function(d) {
                            var result = Object.keys(d.value).map(function(key) {
                              return {'key':key, 'value':d.value[key] };
                            });
                            var data = compute_metrics(result, 1.0);
                            return make_row_object(d.key, data);
                        });
                        var test = compute_metrics(group_testcase_result.all(), 1.0);
                        results.unshift(make_row_object('Selected', test));
                        return results;
                    },
                    top:function(n) {
                        return this.all();
                    },
                    bottom:function(n) {
                        return this.all();
                    }
                };
            }
             