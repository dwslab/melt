            //functions for sorting the evaluation result
            function getSortedEvaluationResult(result){
                switch(result){ 
                    case 'true positive': 
                        return 1;
                    case 'false positive': 
                        return 2; 
                    case 'false negative': 
                        return 3;
                    default:
                        return result;
               }
            }