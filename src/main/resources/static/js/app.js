/* FloodMonitor Webclient - konsumiert die REST-API (JSON und XML). */
(function () {
    "use strict";

    const API_BASE = "/api/v1";
    let refreshTimer = null;

    // ---------- Hilfsfunktionen ----------

    function selectedFormat() {
        return $("#format").val(); // "json" | "xml"
    }

    function acceptType() {
        return selectedFormat() === "xml" ? "application/xml" : "application/json";
    }

    function setStatus(msg, type) {
        $("#status").removeClass("loading error ok").addClass(type || "").text(msg || "");
    }

    function fmtTime(iso) {
        if (!iso) return "-";
        const d = new Date(iso);
        return isNaN(d.getTime()) ? iso : d.toLocaleString("de-AT");
    }

    function badge(level) {
        const lvl = level || "UNKNOWN";
        return '<span class="badge ' + lvl + '">' + lvl + "</span>";
    }

    // ---------- generischer AJAX-Aufruf ----------

    function apiGet(path) {
        const xml = selectedFormat() === "xml";
        return $.ajax({
            url: API_BASE + path,
            method: "GET",
            dataType: xml ? "xml" : "json",
            headers: {
                "Accept": acceptType()
            }
        });
    }

    // ---------- XML-Parser ----------

    function xmlText(node, tag) {
        const el = node.getElementsByTagName(tag)[0];
        return el ? el.textContent : null;
    }

    function xmlToMeasurement(node) {
        return {
            stationId: xmlText(node, "stationId"),
            timestamp: xmlText(node, "timestamp"),
            waterLevel: xmlText(node, "waterLevel"),
            flowRate: xmlText(node, "flowRate"),
            rainfall: xmlText(node, "rainfall"),
            temperature: xmlText(node, "temperature"),
            batteryLevel: xmlText(node, "batteryLevel"),
            status: xmlText(node, "status"),
            warningLevel: xmlText(node, "warningLevel")
        };
    }

    function parseStations(data) {
        if (selectedFormat() === "xml") {
            const nodes = data.getElementsByTagName("station");
            const list = [];
            for (let i = 0; i < nodes.length; i++) {
                list.push({
                    id: xmlText(nodes[i], "id"),
                    name: xmlText(nodes[i], "name"),
                    river: xmlText(nodes[i], "river"),
                    active: xmlText(nodes[i], "active")
                });
            }
            return list;
        }
        return data.stations || [];
    }

    function parseMeasurements(data, tag) {
        if (selectedFormat() === "xml") {
            const nodes = data.getElementsByTagName(tag);
            const list = [];
            for (let i = 0; i < nodes.length; i++) {
                list.push(xmlToMeasurement(nodes[i]));
            }
            return list;
        }
        if (tag === "alert") return data.alerts || [];
        return data.measurements || [];
    }

    function parseLatest(data) {
        if (selectedFormat() === "xml") {
            const node = data.getElementsByTagName("measurement")[0];
            return node ? xmlToMeasurement(node) : null;
        }
        return data;
    }

    // ---------- Rendering ----------

    function loadStations() {
        return apiGet("/stations").done(function (data) {
            const stations = parseStations(data);
            const current = $("#stationSelect").val();
            const $sel = $("#stationSelect").empty();
            stations.forEach(function (s) {
                $sel.append($("<option>").val(s.id).text(s.id + " - " + s.name + " (" + s.river + ")"));
            });
            if (current) $sel.val(current);
        });
    }

    function loadLatest(stationId) {
        return apiGet("/stations/" + stationId + "/measurements/latest")
            .done(function (data) {
                const m = parseLatest(data);
                if (!m) {
                    $("#latestBox").html('<p class="muted">Keine Messung vorhanden.</p>');
                    return;
                }
                const html =
                    '<div class="latest-grid">' +
                    metric("Wasserstand", m.waterLevel + " cm") +
                    metric("Durchfluss", m.flowRate + " m\u00B3/s") +
                    metric("Niederschlag", m.rainfall + " mm/h") +
                    metric("Temperatur", m.temperature + " \u00B0C") +
                    metric("Akku", m.batteryLevel + " %") +
                    metric("Status", m.status) +
                    metric("Warnstufe", badge(m.warningLevel)) +
                    metric("Zeitpunkt", fmtTime(m.timestamp)) +
                    "</div>";
                $("#latestBox").html(html);
            })
            .fail(function (xhr) {
                if (xhr.status === 404) {
                    $("#latestBox").html('<p class="muted">Keine Messung f\u00FCr diese Station.</p>');
                }
            });
    }

    function metric(name, value) {
        return '<div class="metric"><div class="value">' + value +
            '</div><div class="name">' + name + "</div></div>";
    }

    function loadAlerts() {
        return apiGet("/alerts").done(function (data) {
            const alerts = parseMeasurements(data, "alert");
            if (!alerts.length) {
                $("#alertsBox").html('<p class="muted">Aktuell keine Warnungen.</p>');
                return;
            }
            const html = alerts.map(function (a) {
                return '<div class="alert-item"><span>' + a.stationId +
                    " &middot; " + a.waterLevel + " cm</span>" + badge(a.warningLevel) + "</div>";
            }).join("");
            $("#alertsBox").html(html);
        });
    }

    function loadHistory(stationId) {
        const params = [];
        const from = $("#fromFilter").val();
        const to = $("#toFilter").val();
        const warn = $("#warningFilter").val();
        const limit = $("#limitFilter").val();
        if (from) params.push("from=" + encodeURIComponent(new Date(from).toISOString()));
        if (to) params.push("to=" + encodeURIComponent(new Date(to).toISOString()));
        if (warn) params.push("warningLevel=" + warn);
        if (limit) params.push("limit=" + limit);
        const query = params.length ? "?" + params.join("&") : "";

        return apiGet("/stations/" + stationId + "/measurements" + query)
            .done(function (data) {
                const list = parseMeasurements(data, "measurement");
                const $body = $("#historyTable tbody").empty();
                if (!list.length) {
                    $body.append('<tr><td colspan="8" class="muted">Keine Messwerte f\u00FCr die Auswahl.</td></tr>');
                    return;
                }
                list.forEach(function (m) {
                    const lvl = m.warningLevel || "UNKNOWN";
                    $body.append(
                        '<tr class="row-' + lvl + '">' +
                        "<td>" + m.stationId + "</td>" +
                        "<td>" + fmtTime(m.timestamp) + "</td>" +
                        "<td>" + m.waterLevel + "</td>" +
                        "<td>" + m.flowRate + "</td>" +
                        "<td>" + m.rainfall + "</td>" +
                        "<td>" + m.batteryLevel + "</td>" +
                        "<td>" + m.status + "</td>" +
                        "<td>" + badge(lvl) + "</td>" +
                        "</tr>"
                    );
                });
            });
    }

    // ---------- Ablaufsteuerung ----------

    function refreshAll() {
        const stationId = $("#stationSelect").val();
        if (!stationId) {
            setStatus("Bitte Station w\u00E4hlen.", "");
            return;
        }
        setStatus("Lade Daten \u2026", "loading");
        $.when(
            loadLatest(stationId),
            loadAlerts(),
            loadHistory(stationId)
        ).done(function () {
            setStatus("Aktualisiert: " + new Date().toLocaleTimeString("de-AT"), "ok");
        }).fail(function (xhr) {
            let msg = "Fehler beim Laden der Daten.";
            if (xhr) {
                if (xhr.status === 401) msg = "Nicht autorisiert (401) - Zugangsdaten pr\u00FCfen.";
                else if (xhr.status === 403) msg = "Zugriff verweigert (403).";
                else if (xhr.status === 400) msg = "Ung\u00FCltige Anfrage (400) - Filter pr\u00FCfen.";
                else if (xhr.status === 406) msg = "Format nicht unterst\u00FCtzt (406).";
                else if (xhr.status) msg = "Fehler " + xhr.status + ".";
            }
            setStatus(msg, "error");
        });
    }

    function scheduleAutoRefresh() {
        if (refreshTimer) clearInterval(refreshTimer);
        if ($("#autoRefresh").is(":checked")) {
            refreshTimer = setInterval(refreshAll, 10000);
        }
    }

    function init() {
        setStatus("Lade Stationen \u2026", "loading");
        loadStations()
            .done(function () {
                refreshAll();
            })
            .fail(function (xhr) {
                const msg = (xhr && xhr.status === 401)
                    ? "Nicht autorisiert - bitte Zugangsdaten eingeben."
                    : "Stationen konnten nicht geladen werden.";
                setStatus(msg, "error");
            });
        scheduleAutoRefresh();
    }

    // ---------- Events ----------

    $(function () {
        $("#applyBtn, #refreshBtn").on("click", refreshAll);
        $("#stationSelect").on("change", refreshAll);
        $("#format").on("change", refreshAll);
        $("#autoRefresh").on("change", scheduleAutoRefresh);
        $("#username, #password").on("change", init);
        init();
    });
})();
