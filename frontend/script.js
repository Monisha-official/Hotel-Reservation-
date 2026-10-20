// HotelEase - Intelligent Hotel Reservation & Guest Service Platform
// Code Alchemists Team

const API_BASE_URL = "http://localhost:8085";

// ==========================================
// STATE & MAP VARIABLES
// ==========================================

let availableHotels = [];
let locationHierarchyTree = [];
let hasUserSearched = false;

// Leaflet Map state
let leafletMap = null;
let currentPinMarker = null;
let currentRadiusCircle = null;
let hotelMarkersLayerGroup = null;

// Default map coordinates: Brihadisvara Temple, Thanjavur (Tamil Nadu, India)
let currentPinLat = 10.7828;
let currentPinLng = 79.1318;
let currentPinLocationName = "Brihadisvara Temple, Thanjavur";

// Selected room for reservation
let selectedRoomData = null;

// ==========================================
// LEAFLET INTERACTIVE MAP IMPLEMENTATION
// ==========================================

function initLeafletMap() {
  const mapContainer = document.getElementById("interactiveHotelMap");
  if (!mapContainer || typeof L === "undefined") return;

  try {
    // 1. Initialize Map view
    leafletMap = L.map("interactiveHotelMap", {
      zoomControl: true,
      scrollWheelZoom: true
    }).setView([currentPinLat, currentPinLng], 13);

    // 2. OpenStreetMap Tile Layer
    L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
      maxZoom: 19,
      attribution: '© <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
    }).addTo(leafletMap);

    // 3. Custom Destination Pin Icon (Red Pin)
    const pinIcon = L.icon({
      iconUrl: "https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-red.png",
      shadowUrl: "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png",
      iconSize: [25, 41],
      iconAnchor: [12, 41],
      popupAnchor: [1, -34],
      shadowSize: [41, 41]
    });

    // 4. Add Draggable Destination Marker
    currentPinMarker = L.marker([currentPinLat, currentPinLng], {
      draggable: true,
      icon: pinIcon
    }).addTo(leafletMap);

    currentPinMarker.bindPopup(`<strong>📍 Search Destination Pin</strong><br>${currentPinLocationName}`);

    // 5. Marker Drag Event
    currentPinMarker.on("dragend", async () => {
      const pos = currentPinMarker.getLatLng();
      updateMapPinPosition(pos.lat, pos.lng, true);
    });

    // 6. Map Click Event to Reposition Pin
    leafletMap.on("click", (e) => {
      currentPinMarker.setLatLng(e.latlng);
      updateMapPinPosition(e.latlng.lat, e.latlng.lng, true);
    });

    // 7. Layer group for hotel pins
    hotelMarkersLayerGroup = L.layerGroup().addTo(leafletMap);

    // 8. Draw initial radius circle
    drawRadiusCircle(currentPinLat, currentPinLng);

    // 9. Reset Pin Button Event
    const btnReset = document.getElementById("btnResetMapPin");
    if (btnReset) {
      btnReset.onclick = () => {
        updateMapPinPosition(10.7828, 79.1318, false, "Brihadisvara Temple, Thanjavur");
        leafletMap.setView([10.7828, 79.1318], 14);
      };
    }

    // 10. Radius Selector Change Event
    const radiusSelect = document.getElementById("searchRadiusSelect");
    if (radiusSelect) {
      radiusSelect.onchange = () => {
        drawRadiusCircle(currentPinLat, currentPinLng);
        if (hasUserSearched) {
          executeHotelSearch(true);
        }
      };
    }

    // Update display labels
    updateCoordinateDisplay();

  } catch (err) {
    console.error("Leaflet initialization error:", err);
  }
}

function drawRadiusCircle(lat, lng) {
  if (!leafletMap) return;

  const radiusSelect = document.getElementById("searchRadiusSelect");
  const radiusKm = radiusSelect && radiusSelect.value ? parseFloat(radiusSelect.value) : null;

  if (currentRadiusCircle) {
    leafletMap.removeLayer(currentRadiusCircle);
    currentRadiusCircle = null;
  }

  if (radiusKm && radiusKm > 0) {
    currentRadiusCircle = L.circle([lat, lng], {
      radius: radiusKm * 1000, // convert km to meters
      color: "#08d9d6",
      fillColor: "#08d9d6",
      fillOpacity: 0.12,
      weight: 2,
      dashArray: "6, 6"
    }).addTo(leafletMap);
  }
}

async function updateMapPinPosition(lat, lng, doReverseGeocode = false, customLabel = null) {
  currentPinLat = Math.round(lat * 10000) / 10000;
  currentPinLng = Math.round(lng * 10000) / 10000;

  if (currentPinMarker) {
    currentPinMarker.setLatLng([lat, lng]);
  }

  drawRadiusCircle(lat, lng);

  if (customLabel) {
    currentPinLocationName = customLabel;
    updateCoordinateDisplay();
  } else if (doReverseGeocode) {
    // Reverse geocode via OpenStreetMap Nominatim or internal suggestion match
    try {
      const res = await fetch(`https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lng}&format=json&zoom=16`);
      if (res.ok) {
        const data = await res.json();
        currentPinLocationName = data.display_name ? data.display_name.split(",").slice(0, 3).join(",") : `Lat: ${currentPinLat}, Lng: ${currentPinLng}`;
      } else {
        currentPinLocationName = `GPS: (${currentPinLat}, ${currentPinLng})`;
      }
    } catch (e) {
      currentPinLocationName = `GPS: (${currentPinLat}, ${currentPinLng})`;
    }
    updateCoordinateDisplay();
  } else {
    updateCoordinateDisplay();
  }

  if (currentPinMarker) {
    currentPinMarker.setPopupContent(`<strong>📍 Selected Destination</strong><br>${currentPinLocationName}`);
  }

  // If user has already searched, re-query with new coordinates
  if (hasUserSearched) {
    executeHotelSearch(true);
  }
}

function updateCoordinateDisplay() {
  const lblText = document.getElementById("mapSelectedLocationText");
  const lblLat = document.getElementById("mapSelectedLat");
  const lblLng = document.getElementById("mapSelectedLng");
  const badge = document.getElementById("activeLocationBadge");

  if (lblText) lblText.textContent = currentPinLocationName;
  if (lblLat) lblLat.textContent = currentPinLat;
  if (lblLng) lblLng.textContent = currentPinLng;
  if (badge) badge.textContent = `📍 ${currentPinLocationName}`;
}

// Plot hotel markers on Leaflet Map
function plotHotelsOnMap(hotels) {
  if (!leafletMap || !hotelMarkersLayerGroup) return;

  hotelMarkersLayerGroup.clearLayers();

  const hotelIcon = L.icon({
    iconUrl: "https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-blue.png",
    shadowUrl: "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png",
    iconSize: [25, 41],
    iconAnchor: [12, 41],
    popupAnchor: [1, -34],
    shadowSize: [41, 41]
  });

  const boundsGroup = [ [currentPinLat, currentPinLng] ];

  hotels.forEach(h => {
    if (h.latitude && h.longitude) {
      const distInfo = h.distanceKm != null ? `📍 <strong>${h.distanceKm} km</strong> from pin<br>` : '';
      const minPriceInfo = h.minPrice ? `<span style="color:#2ecc71; font-weight:bold;">From ₹${h.minPrice.toLocaleString('en-IN')}/night</span><br>` : '';
      const isBudget = h.minPrice && h.minPrice <= 1000 ? `<span style="background:#e67e22; color:white; padding:1px 6px; border-radius:4px; font-size:0.75em;">BUDGET LODGE</span><br>` : '';

      const marker = L.marker([h.latitude, h.longitude], { icon: hotelIcon });
      marker.bindPopup(`
        <div style="font-size:0.9em; min-width:180px;">
          <h4 style="margin:0 0 4px 0; color:#08d9d6;">🏨 ${h.name}</h4>
          ${isBudget}
          <div style="color:#aaa; font-size:0.85em; margin-bottom:4px;">${h.address} (${h.city})</div>
          ${distInfo}
          ${minPriceInfo}
          <div style="margin-top:6px;">
            <button onclick="scrollToAvailableRooms()" style="background:#08d9d6; color:#111; border:none; padding:4px 10px; border-radius:4px; font-size:0.8em; font-weight:bold; cursor:pointer; width:100%;">
              View Available Rooms
            </button>
          </div>
        </div>
      `);

      hotelMarkersLayerGroup.addLayer(marker);
      boundsGroup.push([h.latitude, h.longitude]);
    }
  });

  // Keep view centered comfortably
  if (boundsGroup.length > 1) {
    try {
      leafletMap.fitBounds(boundsGroup, { padding: [40, 40], maxZoom: 15 });
    } catch (e) {}
  }
}

function scrollToAvailableRooms() {
  const el = document.getElementById("hotelSuggestions");
  if (el) el.scrollIntoView({ behavior: "smooth" });
}

// ==========================================
// 5-TIER ADMINISTRATIVE LOCATION HIERARCHY
// ==========================================

async function loadLocationHierarchy() {
  try {
    const res = await fetch(`${API_BASE_URL}/api/locations/hierarchy`);
    if (!res.ok) return;
    const data = await res.json();
    locationHierarchyTree = data.countries || [];

    populate5TierLocationDropdowns();
    initSmartLocationAutocomplete();
    initGeocodeButton();
  } catch (e) {
    console.error("Error loading location hierarchy:", e);
  }
}

function populate5TierLocationDropdowns() {
  const countrySelect = document.getElementById("searchCountry");
  const stateSelect = document.getElementById("searchState");
  const districtSelect = document.getElementById("searchDistrict");
  const citySelect = document.getElementById("searchLocation");
  const landmarkSelect = document.getElementById("searchLandmark");
  const guestCountrySelect = document.getElementById("guestCountry");

  if (!countrySelect) return;

  // 1. Populate Country Dropdown
  countrySelect.innerHTML = '<option value="">-- All Countries --</option>';
  locationHierarchyTree.forEach(c => {
    const opt = document.createElement("option");
    opt.value = c.name;
    opt.textContent = c.name;
    countrySelect.appendChild(opt);
  });

  // Preselect India if present
  setSelectValue("searchCountry", "India");

  // 1. On Country Change
  countrySelect.onchange = () => {
    const selCountryName = countrySelect.value;

    // Sync guest country
    if (guestCountrySelect && selCountryName) {
      setSelectValue("guestCountry", selCountryName);
    }

    stateSelect.innerHTML = '<option value="">-- All States --</option>';
    if (districtSelect) districtSelect.innerHTML = '<option value="">-- All Districts --</option>';
    citySelect.innerHTML = '<option value="">-- All Cities --</option>';
    if (landmarkSelect) landmarkSelect.innerHTML = '<option value="">-- All Landmarks --</option>';

    if (selCountryName) {
      const countryObj = locationHierarchyTree.find(c => c.name.toLowerCase() === selCountryName.toLowerCase());
      if (countryObj && countryObj.states) {
        countryObj.states.forEach(s => {
          const opt = document.createElement("option");
          opt.value = s.name;
          opt.textContent = s.name;
          stateSelect.appendChild(opt);
        });
        stateSelect.disabled = false;
      }
    } else {
      stateSelect.disabled = true;
    }

    if (districtSelect) districtSelect.disabled = true;
    citySelect.disabled = true;
    if (landmarkSelect) landmarkSelect.disabled = true;

    if (hasUserSearched) executeHotelSearch(true);
  };

  // 2. On State Change
  stateSelect.onchange = () => {
    const selCountryName = countrySelect.value;
    const selStateName = stateSelect.value;

    if (districtSelect) districtSelect.innerHTML = '<option value="">-- All Districts --</option>';
    citySelect.innerHTML = '<option value="">-- All Cities --</option>';
    if (landmarkSelect) landmarkSelect.innerHTML = '<option value="">-- All Landmarks --</option>';

    if (selStateName) {
      const countryObj = locationHierarchyTree.find(c => c.name.toLowerCase() === selCountryName.toLowerCase());
      const stateObj = countryObj?.states?.find(s => s.name.toLowerCase() === selStateName.toLowerCase());

      if (stateObj && stateObj.districts) {
        stateObj.districts.forEach(d => {
          const opt = document.createElement("option");
          opt.value = d.name;
          opt.textContent = d.name;
          districtSelect.appendChild(opt);
        });
        districtSelect.disabled = false;
      }
    } else {
      if (districtSelect) districtSelect.disabled = true;
    }

    citySelect.disabled = true;
    if (landmarkSelect) landmarkSelect.disabled = true;

    if (hasUserSearched) executeHotelSearch(true);
  };

  // 3. On District Change
  if (districtSelect) {
    districtSelect.onchange = () => {
      const selCountryName = countrySelect.value;
      const selStateName = stateSelect.value;
      const selDistrictName = districtSelect.value;

      citySelect.innerHTML = '<option value="">-- All Cities --</option>';
      if (landmarkSelect) landmarkSelect.innerHTML = '<option value="">-- All Landmarks --</option>';

      if (selDistrictName) {
        const countryObj = locationHierarchyTree.find(c => c.name.toLowerCase() === selCountryName.toLowerCase());
        const stateObj = countryObj?.states?.find(s => s.name.toLowerCase() === selStateName.toLowerCase());
        const districtObj = stateObj?.districts?.find(d => d.name.toLowerCase() === selDistrictName.toLowerCase());

        if (districtObj && districtObj.cities) {
          districtObj.cities.forEach(ct => {
            const opt = document.createElement("option");
            opt.value = ct.name;
            opt.textContent = ct.name;
            citySelect.appendChild(opt);
          });
          citySelect.disabled = false;
        }
      } else {
        citySelect.disabled = true;
      }

      if (landmarkSelect) landmarkSelect.disabled = true;

      if (hasUserSearched) executeHotelSearch(true);
    };
  }

  // 4. On City Change
  citySelect.onchange = () => {
    const selCountryName = countrySelect.value;
    const selStateName = stateSelect.value;
    const selDistrictName = districtSelect ? districtSelect.value : "";
    const selCityName = citySelect.value;

    if (landmarkSelect) {
      landmarkSelect.innerHTML = '<option value="">-- All Landmarks --</option>';

      if (selCityName) {
        const countryObj = locationHierarchyTree.find(c => c.name.toLowerCase() === selCountryName.toLowerCase());
        const stateObj = countryObj?.states?.find(s => s.name.toLowerCase() === selStateName.toLowerCase());
        const districtObj = stateObj?.districts?.find(d => d.name.toLowerCase() === selDistrictName.toLowerCase());
        const cityObj = districtObj?.cities?.find(ct => ct.name.toLowerCase() === selCityName.toLowerCase());

        if (cityObj && cityObj.landmarks) {
          cityObj.landmarks.forEach(lm => {
            const opt = document.createElement("option");
            opt.value = lm;
            opt.textContent = lm;
            landmarkSelect.appendChild(opt);
          });
          landmarkSelect.disabled = false;
        }
      } else {
        landmarkSelect.disabled = true;
      }
    }

    // Auto-locate city on map
    if (selCityName) {
      locatePlaceOnMap(`${selCityName}, ${selStateName}, ${selCountryName}`);
    }

    if (hasUserSearched) executeHotelSearch(true);
  };

  // 5. On Landmark Change
  if (landmarkSelect) {
    landmarkSelect.onchange = () => {
      const selLandmark = landmarkSelect.value;
      const selCityName = citySelect.value;
      if (selLandmark) {
        locatePlaceOnMap(`${selLandmark}, ${selCityName}`);
      }
      if (hasUserSearched) executeHotelSearch(true);
    };
  }

  // Trigger initial populate for India -> Tamil Nadu -> Thanjavur if present
  triggerChangeEvent("searchCountry");
  setTimeout(() => {
    setSelectValue("searchState", "Tamil Nadu");
    triggerChangeEvent("searchState");
    setTimeout(() => {
      setSelectValue("searchDistrict", "Thanjavur");
      triggerChangeEvent("searchDistrict");
      setTimeout(() => {
        setSelectValue("searchLocation", "Thanjavur");
        triggerChangeEvent("searchLocation");
      }, 50);
    }, 50);
  }, 50);
}

// Live Autocomplete Suggestions
function initSmartLocationAutocomplete() {
  const input = document.getElementById("smartSearchInput");
  const dropdown = document.getElementById("smartSuggestionsList");
  if (!input || !dropdown) return;

  let debounceTimer;

  input.addEventListener("input", () => {
    clearTimeout(debounceTimer);
    const q = input.value.trim();

    if (q.length < 1) {
      dropdown.style.display = "none";
      return;
    }

    debounceTimer = setTimeout(async () => {
      try {
        const res = await fetch(`${API_BASE_URL}/api/locations/suggestions?q=${encodeURIComponent(q)}`);
        if (!res.ok) return;
        const suggestions = await res.json();

        if (suggestions.length === 0) {
          dropdown.style.display = "none";
          return;
        }

        dropdown.innerHTML = suggestions.map(s => `
          <div class="suggestion-item" 
               data-value="${s.value}" 
               data-city="${s.city || ''}" 
               data-district="${s.district || ''}" 
               data-state="${s.state || ''}" 
               data-country="${s.country || ''}" 
               data-lat="${s.latitude || ''}" 
               data-lng="${s.longitude || ''}" 
               data-type="${s.type}">
            ${s.label}
          </div>
        `).join("");

        dropdown.style.display = "block";

        dropdown.querySelectorAll(".suggestion-item").forEach(el => {
          el.onclick = () => {
            const val = el.getAttribute("data-value");
            const city = el.getAttribute("data-city");
            const district = el.getAttribute("data-district");
            const state = el.getAttribute("data-state");
            const country = el.getAttribute("data-country");
            const lat = parseFloat(el.getAttribute("data-lat"));
            const lng = parseFloat(el.getAttribute("data-lng"));

            input.value = val;
            dropdown.style.display = "none";

            // Reposition map if coordinates exist
            if (!isNaN(lat) && !isNaN(lng)) {
              updateMapPinPosition(lat, lng, false, val);
              if (leafletMap) {
                leafletMap.flyTo([lat, lng], 14, { duration: 1.2 });
              }
            } else {
              locatePlaceOnMap(val);
            }

            // Sync dropdowns if possible
            if (country) setSelectValue("searchCountry", country);
            if (state) setSelectValue("searchState", state);
            if (district) setSelectValue("searchDistrict", district);
            if (city) setSelectValue("searchLocation", city);

            if (hasUserSearched) executeHotelSearch(true);
          };
        });

      } catch (err) {
        console.error("Autocomplete error:", err);
      }
    }, 250);
  });

  document.addEventListener("click", (e) => {
    if (!input.contains(e.target) && !dropdown.contains(e.target)) {
      dropdown.style.display = "none";
    }
  });
}

function initGeocodeButton() {
  const btn = document.getElementById("btnGeocodeSearch");
  const input = document.getElementById("smartSearchInput");
  if (!btn || !input) return;

  btn.onclick = () => {
    const q = input.value.trim();
    if (!q) {
      alert("Please enter a destination to locate on the map.");
      return;
    }
    locatePlaceOnMap(q);
  };
}

async function locatePlaceOnMap(placeQuery) {
  try {
    const res = await fetch(`${API_BASE_URL}/api/locations/geocode?query=${encodeURIComponent(placeQuery)}`);
    if (!res.ok) return;
    const data = await res.json();

    if (data.found && data.latitude && data.longitude) {
      updateMapPinPosition(data.latitude, data.longitude, false, data.name || placeQuery);
      if (leafletMap) {
        leafletMap.flyTo([data.latitude, data.longitude], 14, { duration: 1.2 });
      }
    } else {
      // Fallback geocode via Nominatim directly
      const geoRes = await fetch(`https://nominatim.openstreetmap.org/search?q=${encodeURIComponent(placeQuery)}&format=json&limit=1`);
      if (geoRes.ok) {
        const geoData = await geoRes.json();
        if (geoData && geoData.length > 0) {
          const lat = parseFloat(geoData[0].lat);
          const lon = parseFloat(geoData[0].lon);
          updateMapPinPosition(lat, lon, false, geoData[0].display_name.split(",")[0]);
          if (leafletMap) {
            leafletMap.flyTo([lat, lon], 14, { duration: 1.2 });
          }
        }
      }
    }
  } catch (e) {
    console.warn("Geocoding lookup failed:", e);
  }
}

// Utility dropdown value setter
function setSelectValue(elementId, value) {
  const el = document.getElementById(elementId);
  if (!el || !value) return;
  for (let i = 0; i < el.options.length; i++) {
    if (el.options[i].value.toLowerCase() === value.toLowerCase() ||
        el.options[i].text.toLowerCase().includes(value.toLowerCase())) {
      el.selectedIndex = i;
      break;
    }
  }
}

function triggerChangeEvent(elementId) {
  const el = document.getElementById(elementId);
  if (el) el.dispatchEvent(new Event("change"));
}

// ==========================================
// SEARCH-FIRST AVAILABILITY ENGINE (TASK 3)
// ==========================================

function initSearchEngine() {
  const btnSearch = document.getElementById("btnExecuteSearch");
  if (btnSearch) {
    btnSearch.onclick = () => executeHotelSearch(false);
  }

  // Pre-fill default dates
  const checkinInput = document.getElementById("checkinDate");
  const checkoutInput = document.getElementById("checkoutDate");

  const today = new Date();
  const tomorrow = new Date(today);
  tomorrow.setDate(tomorrow.getDate() + 1);
  const dayAfterTomorrow = new Date(today);
  dayAfterTomorrow.setDate(dayAfterTomorrow.getDate() + 2);

  const todayStr = today.toISOString().split("T")[0];
  const tomorrowStr = tomorrow.toISOString().split("T")[0];
  const dayAfterStr = dayAfterTomorrow.toISOString().split("T")[0];

  if (checkinInput) {
    checkinInput.min = todayStr;
    if (!checkinInput.value) checkinInput.value = tomorrowStr;

    checkinInput.onchange = () => {
      if (checkoutInput) {
        checkoutInput.min = checkinInput.value;
        if (checkoutInput.value && checkoutInput.value <= checkinInput.value) {
          const nextDay = new Date(checkinInput.value);
          nextDay.setDate(nextDay.getDate() + 1);
          checkoutInput.value = nextDay.toISOString().split("T")[0];
        }
      }
      if (hasUserSearched) executeHotelSearch(true);
    };
  }

  if (checkoutInput) {
    checkoutInput.min = tomorrowStr;
    if (!checkoutInput.value) checkoutInput.value = dayAfterStr;

    checkoutInput.onchange = () => {
      if (hasUserSearched) executeHotelSearch(true);
    };
  }

  // Bind filter triggers (Budget, Cooling, Sorting, Amenities)
  const filterElements = [
    "filterBudgetSelect",
    "filterAcSelect",
    "filterSortSelect",
    "filterAmenityWifi",
    "filterAmenityParking",
    "filterAmenityBreakfast",
    "roomType"
  ];

  filterElements.forEach(id => {
    const el = document.getElementById(id);
    if (el) {
      el.onchange = () => {
        if (hasUserSearched) {
          executeHotelSearch(true);
        }
      };
    }
  });
}

// Main execution routine
async function executeHotelSearch(isSilentFilterUpdate = false) {
  const checkin = document.getElementById("checkinDate")?.value;
  const checkout = document.getElementById("checkoutDate")?.value;
  const numGuests = parseInt(document.getElementById("numGuests")?.value || "1");

  // STRICT VALIDATION
  if (!checkin) {
    alert("⚠️ Please select a valid Check-in Date.");
    document.getElementById("checkinDate")?.focus();
    return;
  }

  if (!checkout) {
    alert("⚠️ Please select a valid Check-out Date.");
    document.getElementById("checkoutDate")?.focus();
    return;
  }

  const todayStr = new Date().toISOString().split("T")[0];
  if (checkin < todayStr) {
    alert("⚠️ Check-in date cannot be in the past. Please select today or a future date.");
    return;
  }

  if (checkout <= checkin) {
    alert("⚠️ Check-out date must be strictly after the check-in date.");
    return;
  }

  if (isNaN(numGuests) || numGuests < 1) {
    alert("⚠️ Number of guests must be at least 1.");
    return;
  }

  hasUserSearched = true;

  const btnSearch = document.getElementById("btnExecuteSearch");
  if (btnSearch && !isSilentFilterUpdate) {
    btnSearch.innerHTML = "⏳ Checking Live Inventory & Calculating GPS Distances...";
    btnSearch.disabled = true;
  }

  const container = document.getElementById("hotelCards");
  const countBadge = document.getElementById("searchResultCountBadge");

  // Read location parameters
  const country = document.getElementById("searchCountry")?.value || "";
  const state = document.getElementById("searchState")?.value || "";
  const district = document.getElementById("searchDistrict")?.value || "";
  const city = document.getElementById("searchLocation")?.value || "";
  const landmark = document.getElementById("searchLandmark")?.value || "";
  const query = document.getElementById("smartSearchInput")?.value || "";
  const roomType = document.getElementById("roomType")?.value || "";

  // Radius parameter
  const radiusKm = document.getElementById("searchRadiusSelect")?.value || "";

  // Budget parameters (Task 4)
  const budgetFilter = document.getElementById("filterBudgetSelect")?.value || "all";
  let minPrice = null;
  let maxPrice = null;
  if (budgetFilter === "under1000") maxPrice = 1000;
  else if (budgetFilter === "under2000") maxPrice = 2000;
  else if (budgetFilter === "under3500") maxPrice = 3500;
  else if (budgetFilter === "luxury") minPrice = 4000;

  // Cooling / AC filter
  const acFilter = document.getElementById("filterAcSelect")?.value || "all";
  let hasAc = null;
  if (acFilter === "nonac") hasAc = false;
  else if (acFilter === "ac") hasAc = true;

  // Amenity filters
  const hasWifi = document.getElementById("filterAmenityWifi")?.checked || null;
  const hasParking = document.getElementById("filterAmenityParking")?.checked || null;
  const hasBreakfast = document.getElementById("filterAmenityBreakfast")?.checked || null;

  // Sort parameter
  const sortBy = document.getElementById("filterSortSelect")?.value || "price_asc";

  // Build query URL
  const params = new URLSearchParams();
  if (country) params.append("country", country);
  if (state) params.append("state", state);
  if (district) params.append("district", district);
  if (city) params.append("city", city);
  if (landmark) params.append("landmark", landmark);
  if (query) params.append("query", query);
  if (roomType) params.append("roomType", roomType);
  params.append("checkin", checkin);
  params.append("checkout", checkout);

  // GPS coordinates and radius
  if (currentPinLat && currentPinLng) {
    params.append("destLat", currentPinLat);
    params.append("destLng", currentPinLng);
  }
  if (radiusKm) params.append("radiusKm", radiusKm);

  if (minPrice !== null) params.append("minPrice", minPrice);
  if (maxPrice !== null) params.append("maxPrice", maxPrice);
  if (hasAc !== null) params.append("hasAc", hasAc);
  if (hasWifi) params.append("hasWifi", "true");
  if (hasParking) params.append("hasParking", "true");
  if (hasBreakfast) params.append("hasBreakfast", "true");
  params.append("sortBy", sortBy);

  try {
    const res = await fetch(`${API_BASE_URL}/api/hotels/search-availability?${params.toString()}`);
    if (!res.ok) throw new Error("Search request failed.");
    const hotels = await res.json();

    // Plot pins on map
    plotHotelsOnMap(hotels);

    // Render Room Cards
    renderAvailableRoomCards(hotels, checkin, checkout);

    if (countBadge) {
      let totalRoomsCount = 0;
      hotels.forEach(h => totalRoomsCount += h.roomTypes.length);
      countBadge.innerHTML = `Found <strong>${hotels.length}</strong> properties (<strong>${totalRoomsCount}</strong> available room categories)`;
    }

    if (!isSilentFilterUpdate) {
      scrollToAvailableRooms();
    }

  } catch (err) {
    console.error("Search error:", err);
    if (container) {
      container.innerHTML = `
        <div style="background: rgba(231,76,60,0.15); border: 1px solid #e74c3c; color: #ff6b6b; padding: 25px; border-radius: 12px; text-align: center;">
          <h4>❌ Search Failed</h4>
          <p>Unable to connect to hotel availability service. Please check your backend connection on port 8085.</p>
        </div>
      `;
    }
  } finally {
    if (btnSearch && !isSilentFilterUpdate) {
      btnSearch.innerHTML = "🔍 Search Available Rooms & Calculate Rates";
      btnSearch.disabled = false;
    }
  }
}

// Render dynamic room cards (Task 3 & 4)
function renderAvailableRoomCards(hotels, checkin, checkout) {
  const container = document.getElementById("hotelCards");
  if (!container) return;

  if (!hotels || hotels.length === 0) {
    container.innerHTML = `
      <div style="background: rgba(241,196,15,0.15); border: 1px solid #f1c40f; color: #f1c40f; padding: 35px 20px; border-radius: 14px; text-align: center;">
        <div style="font-size: 2.2em; margin-bottom: 8px;">🏨</div>
        <h4 style="font-size: 1.2em; margin-bottom: 6px;">No Accommodations Found</h4>
        <p style="color: #ddd; max-width: 550px; margin: 0 auto; font-size: 0.9em;">
          No properties match your current radius or budget filters. Try increasing the search radius, switching cooling to "All", or selecting a nearby town.
        </p>
      </div>
    `;
    return;
  }

  let html = `<div class="room-card-grid">`;

  hotels.forEach(hotel => {
    hotel.roomTypes.forEach(rt => {
      // Inventory Status Badge
      let availBadge = "";
      if (rt.availableRooms > 0 && rt.availableRooms <= 3) {
        availBadge = `<div class="room-card-avail-badge badge-low-stock">🔥 ONLY ${rt.availableRooms} ROOM${rt.availableRooms > 1 ? 'S' : ''} LEFT!</div>`;
      } else if (rt.availableRooms > 3) {
        availBadge = `<div class="room-card-avail-badge badge-available">⚡ ${rt.availableRooms} ROOMS AVAILABLE</div>`;
      } else {
        availBadge = `<div class="room-card-avail-badge badge-soldout">🔒 SOLD OUT</div>`;
      }

      // Budget Badge (Task 4: Highlighting rooms under ₹1,000)
      let budgetBadge = "";
      if (rt.price <= 1000) {
        budgetBadge = `<span style="background: #e67e22; color: white; padding: 2px 8px; border-radius: 4px; font-size: 0.75em; font-weight: bold; margin-left: 6px;">💰 BUDGET / PILGRIM LODGE</span>`;
      }

      // Distance Badge
      const distBadge = hotel.distanceKm != null 
        ? `<div style="font-size: 0.85em; color: #08d9d6; font-weight: 600; margin-top: 2px;">📍 ${hotel.distanceKm} km from selected destination pin</div>`
        : `<div style="font-size: 0.85em; color: #aaa; margin-top: 2px;">📍 ${hotel.address} (${hotel.city})</div>`;

      // Room Cooling
      const coolingChip = rt.hasAc
        ? `<span class="room-feature-chip" style="border-color:#08d9d6; color:#08d9d6;">❄️ Air Conditioned</span>`
        : `<span class="room-feature-chip" style="border-color:#2ecc71; color:#2ecc71;">🍃 Non-AC Budget</span>`;

      // Sample room image fallback
      const roomImg = hotel.imageUrl || "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=500";

      // Transparent pricing calculations
      const nights = rt.nights || 1;
      const subtotal = rt.totalStayCost || (rt.price * nights);
      const tax = rt.taxAmount || (subtotal * 0.18);
      const grandTotal = rt.grandTotal || (subtotal + tax);

      const hotelObjJson = encodeURIComponent(JSON.stringify(hotel));
      const rtObjJson = encodeURIComponent(JSON.stringify(rt));

      html += `
        <div class="room-card ${rt.price <= 1000 ? 'budget-room-card' : ''}">
          <div class="room-card-img-wrapper">
            <img src="${roomImg}" alt="${rt.name}" loading="lazy" />
            <div class="room-card-hotel-badge">🏨 ${hotel.name}</div>
            ${availBadge}
          </div>
          <div class="room-card-content">
            <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 2px;">
              <div>
                <span class="room-card-title">${rt.name}</span>
                ${budgetBadge}
              </div>
              <span style="color: #08d9d6; font-weight: bold; font-size: 0.95em;">★ ${hotel.rating || 4.2}</span>
            </div>

            ${distBadge}

            <!-- Price Breakdown Strip -->
            <div class="room-card-price-row" style="margin: 12px 0 10px 0;">
              <div>
                <div class="room-card-price">₹${rt.price.toLocaleString('en-IN')} <span>/ night</span></div>
                <div style="font-size: 0.78em; color: #aaa;">Transparent Rate: ${nights} night(s) = ₹${subtotal.toLocaleString('en-IN')} + 18% GST = <strong>₹${grandTotal.toLocaleString('en-IN')}</strong> Total</div>
              </div>
            </div>

            <!-- Features -->
            <div class="room-card-features">
              ${coolingChip}
              <span class="room-feature-chip">👥 Max ${rt.capacityAdults} Adults + ${rt.capacityChildren} Child</span>
              ${hotel.hasWifi ? '<span class="room-feature-chip">📶 Free WiFi</span>' : ''}
              ${hotel.hasParking ? '<span class="room-feature-chip">🚗 Free Parking</span>' : ''}
              ${hotel.hasBreakfast ? '<span class="room-feature-chip">🍳 Breakfast Included</span>' : ''}
            </div>

            <!-- Action Buttons -->
            <div style="display: flex; gap: 8px; margin-top: auto; padding-top: 10px;">
              <button type="button" onclick="centerHotelOnMap(${hotel.latitude}, ${hotel.longitude}, '${hotel.name.replace(/'/g, "\\'")}')" class="room-card-btn" style="background: rgba(8,217,214,0.15); color: #08d9d6; border: 1px solid #08d9d6; flex: 1; padding: 10px 6px; font-size: 0.85em;">
                🗺️ Pin on Map
              </button>
              ${rt.isAvailable ? `
                <button type="button" onclick="selectRoomForReservation('${hotelObjJson}', '${rtObjJson}')" class="room-card-btn room-card-btn-active" style="flex: 2; padding: 10px;">
                  Reserve Room ➔
                </button>
              ` : `
                <button disabled class="room-card-btn room-card-btn-disabled" style="flex: 2; padding: 10px;">
                  Sold Out
                </button>
              `}
            </div>
          </div>
        </div>
      `;
    });
  });

  html += `</div>`;
  container.innerHTML = html;
}

// Map center on specific hotel
function centerHotelOnMap(lat, lng, name) {
  if (leafletMap && lat && lng) {
    leafletMap.flyTo([lat, lng], 16, { duration: 1.0 });
    const mapEl = document.getElementById("interactiveHotelMap");
    if (mapEl) mapEl.scrollIntoView({ behavior: "smooth" });
  }
}

// Select Room and Scroll to Reservation Form
function selectRoomForReservation(hotelJsonEncoded, rtJsonEncoded) {
  try {
    const hotel = JSON.parse(decodeURIComponent(hotelJsonEncoded));
    const rt = JSON.parse(decodeURIComponent(rtJsonEncoded));
    selectedRoomData = { hotel, rt };

    // Update display banner
    const titleEl = document.getElementById("selectedRoomDisplayTitle");
    const locEl = document.getElementById("selectedRoomDisplayLocation");
    const priceEl = document.getElementById("selectedRoomDisplayPrice");
    const hotelIdInput = document.getElementById("selectedHotelId");
    const rtIdInput = document.getElementById("selectedRoomTypeId");
    const rtBasePriceInput = document.getElementById("selectedRoomBasePrice");

    const nights = rt.nights || 1;
    const grandTotal = rt.grandTotal || (rt.price * nights * 1.18);

    if (titleEl) titleEl.innerHTML = `🏨 ${hotel.name} — <strong>${rt.name}</strong> ${rt.price <= 1000 ? '(Budget Accommodation)' : ''}`;
    if (locEl) locEl.innerHTML = `📍 ${hotel.address} (${hotel.city}, ${hotel.country})`;
    if (priceEl) priceEl.innerHTML = `₹${rt.price.toLocaleString('en-IN')} <span style="font-size:0.7em; color:#aaa;">/ night</span><br><span style="font-size:0.8em; color:#2ecc71;">₹${grandTotal.toLocaleString('en-IN')} Total (${nights}N incl. GST)</span>`;

    if (hotelIdInput) hotelIdInput.value = hotel.id;
    if (rtIdInput) rtIdInput.value = rt.id;
    if (rtBasePriceInput) rtBasePriceInput.value = rt.price;

    // Scroll down to reservation form
    const formSection = document.getElementById("bookingReservationSection");
    if (formSection) {
      formSection.scrollIntoView({ behavior: "smooth" });
    }

  } catch (e) {
    console.error("Room selection error:", e);
  }
}


function initBookingForm() {
  const form = document.getElementById("bookingForm");
  if (!form) return;

  form.addEventListener("submit", async (e) => {
    e.preventDefault();

    const selectedHotelId = document.getElementById("selectedHotelId")?.value;
    const selectedRoomTypeId = document.getElementById("selectedRoomTypeId")?.value;

    if (!selectedRoomData && (!selectedHotelId || !selectedRoomTypeId)) {
      alert("⚠️ Please select an available room by clicking 'Reserve Room ➔' on any room card above.");
      scrollToAvailableRooms();
      return;
    }

    const checkin = document.getElementById("checkinDate")?.value;
    const checkout = document.getElementById("checkoutDate")?.value;

    if (!checkin || !checkout) {
      alert("⚠️ Please select your check-in and check-out dates.");
      return;
    }

    const submitBtn = form.querySelector('button[type="submit"]');
    const originalBtnText = submitBtn.innerHTML;
    submitBtn.innerHTML = '⏳ Processing Reservation...';
    submitBtn.disabled = true;

    const bookingPayload = {
      guestName: document.getElementById("guestName")?.value.trim(),
      guestEmail: document.getElementById("guestEmail")?.value.trim(),
      guestPhone: document.getElementById("guestPhone")?.value.trim(),
      guestCountry: document.getElementById("guestCountry")?.value || "India",
      hotelId: selectedHotelId ? parseInt(selectedHotelId) : (selectedRoomData?.hotel?.id || 1),
      roomTypeId: selectedRoomTypeId ? parseInt(selectedRoomTypeId) : (selectedRoomData?.rt?.id || 1),
      roomType: selectedRoomData?.rt?.name || "Standard Room",
      location: selectedRoomData?.hotel?.city || document.getElementById("searchLocation")?.value || "Thanjavur",
      checkinDate: checkin,
      checkoutDate: checkout,
      numRooms: parseInt(document.getElementById("numRooms")?.value || "1"),
      numGuests: parseInt(document.getElementById("numGuests")?.value || "1"),
      numChildren: parseInt(document.getElementById("numChildren")?.value || "0"),
      bedPreference: document.getElementById("bedPreference")?.value,
      floorPreference: document.getElementById("floorPreference")?.value,
      viewPreference: document.getElementById("viewPreference")?.value,
      smokingPreference: document.getElementById("smokingPreference")?.value,
      airportPickup: document.getElementById("airportPickup")?.checked || false,
      breakfast: document.getElementById("breakfast")?.checked || false,
      wifi: document.getElementById("wifi")?.checked || false,
      parking: document.getElementById("parking")?.checked || false,
      spa: document.getElementById("spa")?.checked || false,
      visitPurpose: document.getElementById("visitPurpose")?.value,
      arrivalTime: document.getElementById("arrivalTime")?.value,
      specialRequests: document.getElementById("specialRequests")?.value.trim(),
      paymentMethod: document.getElementById("paymentMethod")?.value
    };


    try {
      const res = await fetch(`${API_BASE_URL}/api/bookings`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(bookingPayload)
      });

      const data = await res.json();

      if (!res.ok || !data.success) {
        alert(`❌ Reservation Failed: ${data.message || "Unknown error"}`);
        submitBtn.innerHTML = originalBtnText;
        submitBtn.disabled = false;
        return;
      }

      // Launch Gateway interface or Pay at Hotel confirmation
      if (bookingPayload.paymentMethod !== "payathotel") {
        openPaymentGatewayModal(data.bookingId, data.bookingCode, data.totalCost, bookingPayload);
      } else {
        showPaymentSuccessModal({
          bookingId: data.bookingId,
          bookingCode: data.bookingCode,
          totalCost: data.totalCost,
          qrCodeToken: data.qrCodeToken,
          guestName: bookingPayload.guestName,
          guestEmail: bookingPayload.guestEmail,
          location: bookingPayload.location,
          roomType: bookingPayload.roomType,
          checkinDate: bookingPayload.checkinDate,
          checkoutDate: bookingPayload.checkoutDate,
          status: "CONFIRMED (Pay at Hotel)"
        });
      }

    } catch (err) {
      console.error("Booking error:", err);
      alert("❌ Server connection error. Please ensure Spring Boot backend is running.");
    } finally {
      submitBtn.innerHTML = originalBtnText;
      submitBtn.disabled = false;
    }
  });
}

// Secure Online Payment Modal (Razorpay/Stripe Gateway Integration)
function openPaymentGatewayModal(bookingId, bookingCode, amount, bookingPayload) {
  let modal = document.getElementById("paymentGatewayModal");
  if (!modal) {
    modal = document.createElement("div");
    modal.id = "paymentGatewayModal";
    modal.style.cssText = "position:fixed; top:0; left:0; width:100%; height:100%; background:rgba(0,0,0,0.8); z-index:10000; display:flex; justify-content:center; align-items:center;";
    document.body.appendChild(modal);
  }

  modal.innerHTML = `
    <div style="background:#1e2a38; border:1px solid #08d9d6; border-radius:16px; width:90%; max-width:480px; padding:30px; color:white; box-shadow:0 12px 40px rgba(0,0,0,0.6);">
      <div style="display:flex; justify-content:space-between; align-items:center; border-bottom:1px solid rgba(8,217,214,0.3); padding-bottom:12px; margin-bottom:20px;">
        <h3 style="color:#08d9d6; margin:0;">🔒 Razorpay / Stripe Gateway</h3>
        <span onclick="document.getElementById('paymentGatewayModal').style.display='none'" style="cursor:pointer; font-size:24px;">&times;</span>
      </div>
      <p style="margin-bottom:15px; opacity:0.9;">Completing transaction for Booking <strong>${bookingCode}</strong></p>
      <div style="background:rgba(255,255,255,0.05); padding:15px; border-radius:10px; margin-bottom:20px;">
        <div style="display:flex; justify-content:space-between; font-size:1.2em; font-weight:bold; color:#08d9d6;">
          <span>Payable Amount:</span>
          <span>₹${amount.toLocaleString('en-IN')}</span>
        </div>
        <p style="font-size:0.8em; color:#aaa; margin-top:5px;">Protected with 256-bit SSL encryption. Card numbers are never stored in hotel databases.</p>
      </div>

      <div style="margin-bottom:20px;">
        <label style="display:block; margin-bottom:8px; font-size:0.9em; color:#08d9d6;">Payment Verification Actions</label>
        <button id="btnCompletePaymentSuccess" class="btn" style="width:100%; margin-bottom:10px; background:#2ecc71;">
          ✅ Complete Payment (Simulate Gateway Success)
        </button>
        <button id="btnCompletePaymentFailed" class="btn" style="width:100%; background:#e74c3c;">
          ❌ Fail Payment (Test Error Handling)
        </button>
      </div>
    </div>
  `;

  modal.style.display = "flex";

  document.getElementById("btnCompletePaymentSuccess").onclick = () => {
    processGatewayVerification(bookingId, bookingCode, amount, bookingPayload, "pay_simulated_success");
  };

  document.getElementById("btnCompletePaymentFailed").onclick = () => {
    processGatewayVerification(bookingId, bookingCode, amount, bookingPayload, "pay_simulated_failed");
  };
}

async function processGatewayVerification(bookingId, bookingCode, amount, bookingPayload, paymentId) {
  const modal = document.getElementById("paymentGatewayModal");

  try {
    const res = await fetch(`${API_BASE_URL}/api/payments/verify`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        bookingId: bookingId,
        razorpayOrderId: "order_HE_" + bookingId,
        razorpayPaymentId: paymentId,
        razorpaySignature: "sig_simulated_" + Date.now()
      })
    });

    const result = await res.json();
    if (modal) modal.style.display = "none";

    if (result.success) {
      showPaymentSuccessModal({
        bookingId: bookingId,
        bookingCode: bookingCode,
        totalCost: amount,
        qrCodeToken: "QR-" + bookingCode,
        guestName: bookingPayload?.guestName || "Guest",
        guestEmail: bookingPayload?.guestEmail || "",
        location: bookingPayload?.location || "HotelEase Resort",
        roomType: bookingPayload?.roomType || "Standard Room",
        checkinDate: bookingPayload?.checkinDate || "",
        checkoutDate: bookingPayload?.checkoutDate || "",
        status: "CONFIRMED & PAID"
      });
    } else {
      alert(`❌ Payment Failure: ${result.message}`);
    }
  } catch (err) {
    alert("❌ Error verifying payment on backend: " + err.message);
  }
}

// Payment Success & Booking Confirmation Modal
function showPaymentSuccessModal(details) {
  let modal = document.getElementById("paymentSuccessModal");
  if (!modal) {
    modal = document.createElement("div");
    modal.id = "paymentSuccessModal";
    modal.style.cssText = "position:fixed; top:0; left:0; width:100%; height:100%; background:rgba(0,0,0,0.85); z-index:10001; display:flex; justify-content:center; align-items:center;";
    document.body.appendChild(modal);
  }

  modal.innerHTML = `
    <div style="background: linear-gradient(135deg, #1c2b36, #0f2027); border: 2px solid #08d9d6; border-radius: 20px; width: 90%; max-width: 580px; padding: 35px; color: white; box-shadow: 0 16px 50px rgba(0,0,0,0.7); text-align: left;">
      <div style="text-align: center; margin-bottom: 25px;">
        <div style="font-size: 50px; margin-bottom: 10px;">🎉</div>
        <h2 style="color: #08d9d6; margin: 0; font-size: 1.8em;">PAYMENT SUCCESSFUL!</h2>
        <p style="color: #2ecc71; font-weight: bold; margin-top: 5px; font-size: 1.1em;">Reservation Guaranteed & Confirmed</p>
      </div>

      <div style="background: rgba(255,255,255,0.05); padding: 20px; border-radius: 14px; border: 1px solid rgba(8,217,214,0.2); margin-bottom: 25px;">
        <div style="display:flex; justify-content:space-between; margin-bottom: 10px;">
          <span style="color:#aaa;">Booking Reference Code:</span>
          <span style="font-weight:bold; color:#08d9d6; font-size:1.1em;">${details.bookingCode}</span>
        </div>
        <div style="display:flex; justify-content:space-between; margin-bottom: 8px;">
          <span style="color:#aaa;">Guest Name:</span>
          <span>${details.guestName} (${details.guestEmail})</span>
        </div>
        <div style="display:flex; justify-content:space-between; margin-bottom: 8px;">
          <span style="color:#aaa;">Hotel Location:</span>
          <span>${details.location}</span>
        </div>
        <div style="display:flex; justify-content:space-between; margin-bottom: 8px;">
          <span style="color:#aaa;">Room Selection:</span>
          <span>${details.roomType}</span>
        </div>
        <div style="display:flex; justify-content:space-between; margin-bottom: 8px;">
          <span style="color:#aaa;">Check-in / Check-out:</span>
          <span>${details.checkinDate} ➔ ${details.checkoutDate}</span>
        </div>
        <hr style="border:1px dashed rgba(255,255,255,0.2); margin: 12px 0;">
        <div style="display:flex; justify-content:space-between; font-size: 1.2em; font-weight: bold;">
          <span>Total Amount Paid:</span>
          <span style="color:#08d9d6;">₹${details.totalCost.toLocaleString('en-IN')}</span>
        </div>
      </div>

      <!-- Digital QR Check-in Pass -->
      <div style="background: white; color: black; padding: 15px; border-radius: 12px; text-align: center; margin-bottom: 25px;">
        <div style="font-weight: bold; font-size: 0.9em; margin-bottom: 5px; color:#2c5364;">📲 CONTACTLESS QR CHECK-IN PASS</div>
        <div style="font-family: monospace; font-size: 1.2em; font-weight: bold; letter-spacing: 2px; color:#00adb5;">${details.qrCodeToken}</div>
        <div style="font-size: 0.75em; color: #666; margin-top: 3px;">Present this digital code at front desk upon arrival for instant key release</div>
      </div>

      <div style="display: flex; gap: 15px;">
        <button onclick="window.print()" class="btn" style="flex: 1; background: #00adb5;">
          🖨️ Print Pass
        </button>
        <button onclick="window.location.href='admin.html'" class="btn" style="flex: 1; background: #9b59b6;">
          📊 View Admin Desk
        </button>
        <button onclick="window.location.reload()" class="btn" style="flex: 1; background: #2ecc71;">
          ✨ Done
        </button>
      </div>
    </div>
  `;

  modal.style.display = "flex";
}

// ==========================================
// CONSOLIDATED INTELLIGENT CHATBOT ENGINE
// ==========================================

function initChatbot() {
  const chatbot = document.getElementById("chatbot");
  const toggleBtn = document.getElementById("chatbot-toggle");
  const closeBtn = document.getElementById("chatbot-close");
  const sendBtn = document.getElementById("chatbot-send");
  const input = document.getElementById("chatbot-input");
  const messages = document.getElementById("chatbot-messages");

  if (!toggleBtn || !chatbot) return;

  toggleBtn.onclick = () => chatbot.style.display = "flex";
  if (closeBtn) closeBtn.onclick = () => chatbot.style.display = "none";

  const handleSend = async () => {
    const text = input.value.trim();
    if (!text) return;

    // Append User Message
    const userDiv = document.createElement("div");
    userDiv.className = "user-message";
    userDiv.textContent = text;
    messages.appendChild(userDiv);

    input.value = "";
    messages.scrollTop = messages.scrollHeight;

    // Append Bot Thinking Message
    const botDiv = document.createElement("div");
    botDiv.className = "bot-message";
    botDiv.textContent = "🤖 AI is querying hotel database...";
    messages.appendChild(botDiv);
    messages.scrollTop = messages.scrollHeight;

    try {
      const res = await fetch(`${API_BASE_URL}/api/chat`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ message: text })
      });

      if (!res.ok) throw new Error("Chat server error");

      const data = await res.json();
      botDiv.innerHTML = formatAIResponseText(data.reply);

    } catch (err) {
      botDiv.textContent = "❌ Backend server unreachable. Make sure Spring Boot server is running on port 8085.";
    }

    messages.scrollTop = messages.scrollHeight;
  };

  if (sendBtn) sendBtn.onclick = handleSend;
  if (input) {
    input.addEventListener("keypress", e => {
      if (e.key === "Enter") handleSend();
    });
  }
}

function formatAIResponseText(text) {
  if (!text) return "<i>No response received.</i>";
  return text.replace(/\n/g, "<br>").replace(/\*\*(.*?)\*\*/g, "<strong>$1</strong>");
}

// ==========================================
// DYNAMIC ROLE-BASED NAVBAR USER STATUS
// ==========================================

function updateNavbarUserStatus() {
  const navUl = document.querySelector("header.navbar nav ul");
  if (!navUl) return;

  const currentUser = JSON.parse(localStorage.getItem("currentUser") || "null");

  // Clear existing navbar auth container if present
  const existingAuthContainer = document.getElementById("navAuthContainer");
  if (existingAuthContainer) existingAuthContainer.remove();

  if (currentUser) {
    const role = (currentUser.role || "").toUpperCase();
    const displayName = currentUser.name || currentUser.fullName || currentUser.email || "User";

    // Remove static Admin link if present to avoid duplication
    const staticAdminLink = navUl.querySelector('a[href="admin.html"]');
    if (staticAdminLink && staticAdminLink.parentElement) {
      staticAdminLink.parentElement.remove();
    }

    const authContainer = document.createElement("li");
    authContainer.id = "navAuthContainer";
    authContainer.style.cssText = "display: inline-flex; align-items: center; gap: 10px; margin-left: 10px;";

    let navContentHTML = '';
    
    // User Dashboard Link
    navContentHTML += `<a href="user-dashboard.html" style="color: white; text-decoration: none; font-weight: 500;">My Dashboard</a>`;
    
    // Admin Desk Link if role is ADMIN or STAFF
    if (role === "ADMIN" || role === "STAFF") {
      navContentHTML += `<a href="admin.html" style="color: #08d9d6; font-weight: bold; text-decoration: none;">Admin Desk</a>`;
    }

    // User Profile Badge
    navContentHTML += `
      <span style="font-size: 0.85em; background: rgba(8,217,214,0.15); border: 1px solid #08d9d6; color: #08d9d6; padding: 4px 10px; border-radius: 20px; font-weight: 600;">
        ${role === "ADMIN" ? "👑 Admin: " : "👤 "}${displayName}
      </span>
      <button onclick="handleGlobalLogout()" style="background: #e74c3c; color: white; border: none; padding: 5px 12px; border-radius: 6px; cursor: pointer; font-size: 0.85em; font-weight: bold; transition: background 0.3s;">
        Logout
      </button>
    `;

    authContainer.innerHTML = navContentHTML;
    navUl.appendChild(authContainer);

  } else {
    // Guest user - remove static Admin link for guests if security preferred
    const staticAdminLink = navUl.querySelector('a[href="admin.html"]');
    if (staticAdminLink && staticAdminLink.parentElement) {
      staticAdminLink.parentElement.remove();
    }

    let signinLink = navUl.querySelector('a[href="login.html"]');
    if (!signinLink) {
      const authContainer = document.createElement("li");
      authContainer.id = "navAuthContainer";
      authContainer.innerHTML = `<a href="login.html" class="btn" style="padding: 6px 16px; margin-left: 10px;">Sign In</a>`;
      navUl.appendChild(authContainer);
    }
  }
}

function handleGlobalLogout() {
  localStorage.removeItem("currentUser");
  window.location.href = "login.html";
}

// Initialize on DOM load
document.addEventListener("DOMContentLoaded", () => {
  initLeafletMap();
  loadLocationHierarchy();
  initSearchEngine();
  initBookingForm();
  initChatbot();
  updateNavbarUserStatus();
});

