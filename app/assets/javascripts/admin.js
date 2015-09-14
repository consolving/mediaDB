var color = d3.scale.category20c();
var width = 500,
        height = 250,
        radius = Math.min(width, height) / 3;

var pie = d3.layout.pie()
        .sort(null)
        .value(function (d) {
            return d.value;
        });

var arc = d3.svg.arc()
        .outerRadius(radius * 0.8)
        .innerRadius(radius * 0.4);

var outerArc = d3.svg.arc()
        .innerRadius(radius * 0.9)
        .outerRadius(radius * 0.9);


var key = function (d) {
    return d.data.label;
};

var alpha = 0.5;
var spacing = 22;

function drawPie(data, graphId) {

    var svg = d3.select(graphId)
            .append("svg")
            .append("g");
    svg.append("g")
            .attr("class", "slices");
    svg.append("g")
            .attr("class", "labels");
    svg.append("g")
            .attr("class", "lines");
    var labelPositions = {};

    svg.attr("transform", "translate(" + width / 2 + "," + height / 2 + ")");
    console.log(JSON.stringify(data));
    /* ------- PIE SLICES -------*/
    var slice = svg.select(".slices").selectAll("path.slice").data(pie(data), key);

    slice.enter()
            .insert("path")
            .style("fill", function (d) {
                return color(d.data.label);
            })
            .attr("class", "slice");

    slice
            .transition().duration(1000)
            .attrTween("d", function (d) {
                this._current = this._current || d;
                var interpolate = d3.interpolate(this._current, d);
                this._current = interpolate(0);
                return function (t) {
                    return arc(interpolate(t));
                };
            });

    slice.exit()
            .remove();

    /* ------- TEXT LABELS -------*/

    var textId = 1;
    var text = svg.select(".labels").selectAll("text").data(pie(data), key);
    text.enter()
            .append("text")
            .attr("dy", ".35em")
            .attr("id", function (d) {
                return "text-" + textId++;
            })
            .text(function (d) {
                return d.data.label;
            });
    svg.select(".labels").selectAll("text").each(insertLinebreaks);
    function midAngle(d) {
        return d.startAngle + (d.endAngle - d.startAngle) / 2;
    }

    text.transition().duration(1000)
            .attrTween("transform", function (d) {
                this._current = this._current || d;
                var interpolate = d3.interpolate(this._current, d);
                this._current = interpolate(0);
                return function (t) {
                    var d2 = interpolate(t);
                    var pos = outerArc.centroid(d2);
                    pos[0] = radius * (midAngle(d2) < Math.PI ? 1 : -1);
                    return "translate(" + pos + ")";
                };
            })
            //.each("end", function () {
            //relax(graphId);
            //})
            .styleTween("text-anchor", function (d) {
                this._current = this._current || d;
                var interpolate = d3.interpolate(this._current, d);
                this._current = interpolate(0);
                return function (t) {
                    var d2 = interpolate(t);
                    return midAngle(d2) < Math.PI ? "start" : "end";
                };
            });
    text.exit()
            .remove();

    /* ------- SLICE TO TEXT POLYLINES -------*/

    var polyline = svg.select(".lines").selectAll("polyline").data(pie(data), key);
    polyline.enter()
            .append("polyline");

    polyline.transition().duration(1000)
            .attrTween("points", function (d) {
                this._current = this._current || d;
                var interpolate = d3.interpolate(this._current, d);
                this._current = interpolate(0);
                return function (t) {
                    var d2 = interpolate(t);
                    var pos = outerArc.centroid(d2);
                    pos[0] = radius * 0.95 * (midAngle(d2) < Math.PI ? 1 : -1);
                    return [arc.centroid(d2), outerArc.centroid(d2), pos];
                };
            });

    polyline.exit()
            .remove();
}

function relax(graphId) {
    var svg = d3.select(graphId);
    var text = svg.select(".labels").selectAll("text");
    again = false;
    text.each(function (d, i) {
        a = this;
        da = d3.select(a);
        y1 = da.attr("y");
        text.each(function (d, j) {
            b = this;
            // a & b are the same element and don't collide.
            if (a.id === b.id)
                return;
            db = d3.select(b);
            // a & b are on opposite sides of the chart and
            // don't collide
            if (da.attr("text-anchor") !== db.attr("text-anchor"))
                return;
            // Now let's calculate the distance between
            // these elements. 
            y2 = db.attr("y");
            deltaY = y1 - y2;

            // If spacing is greater than our specified spacing,
            // they don't collide.
            if (Math.abs(deltaY) > spacing)
                return;

            // If the labels collide, we'll push each 
            // of the two labels up and down a little bit.
            again = true;
            sign = deltaY > 0 ? 1 : -1;
            adjust = sign * alpha;
            da.attr("y", +y1 + adjust);
            db.attr("y", +y2 - adjust);
        });

    });

    if (again) {
        setTimeout(function () {
            relax(graphId);
        }, 20);
    }
}

var insertLinebreaks = function (d) {
    var el = d3.select(this);
    var words = d.data.label.split("\n");
    el.text('');
    for (var i = 0; i < words.length; i++) {
        var tspan = el.append('tspan').text(words[i]);
        if (i > 0)
            tspan.attr('x', 0).attr('dy', '15');
    }
};

function drawTable(data, graphId) {
    var template = "<tr><th>##label##</th><td>##value##</td></tr>";
    var table = $('<table class="table table-striped"></table>');    
    $.each(data, function (i, d) {
        table.append(template.replace("##label##", d.label).replace("##value##", d.value));
    });
    $(graphId).append(table);
}