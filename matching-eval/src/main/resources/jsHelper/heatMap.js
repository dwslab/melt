            //functions for calculating confusion matrix heatmaps
            function getTrueCondition(result){
                switch(result){ 
                    case 'true positive': 
                        return 'cond positive';
                    case 'false positive': 
                        return 'cond negative'; 
                    case 'false negative': 
                        return 'cond positive';
                    case 'true negative': 
                        return 'cond negative';
               }
               return 'default';
            }
            function getPredictedCondition(result){
                switch(result){ 
                    case 'true positive': 
                        return 'positive';
                    case 'false positive': 
                        return 'positive'; 
                    case 'false negative': 
                        return 'negative';
                    case 'true negative': 
                        return 'negative';
               }
               return 'default';
            }