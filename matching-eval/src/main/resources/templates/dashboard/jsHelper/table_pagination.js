            var ofs = 0, pag = 30;

            function update_offset() {
                var totFilteredRecs = ndx.groupAll().value();
                var end = ofs + pag > totFilteredRecs ? totFilteredRecs : ofs + pag;
                ofs = ofs >= totFilteredRecs ? Math.floor((totFilteredRecs - 1) / pag) * pag : ofs;
                ofs = ofs < 0 ? 0 : ofs;

                dataTable.beginSlice(ofs);
                dataTable.endSlice(ofs+pag);
            }
            function display() {
                var totFilteredRecs = ndx.groupAll().value();
                var end = ofs + pag > totFilteredRecs ? totFilteredRecs : ofs + pag;
                d3.select('#begin')
                    .text(end === 0? ofs : ofs + 1);
                d3.select('#end')
                    .text(end);
                d3.select('#last')
                    .attr('disabled', ofs-pag<0 ? 'true' : null);
                d3.select('#next')
                    .attr('disabled', ofs+pag>=totFilteredRecs ? 'true' : null);
            }
            function next() {
                ofs += pag;
                update_offset();
                dataTable.redraw();
            }
            function last() {
                ofs -= pag;
                update_offset();
                dataTable.redraw();
            }