            //fake groups (e.g. to reduce the empty bins)
            function remove_empty_bins(source_group) {
                return {
                    all:function () {
                        return source_group.all().filter(function(d) {
                            const values = Object.values(d.value)
                            for (const value of values) {
                                if(value)
                                    return true;
                            }
                            return false;
                        });
                    }
                };
            }