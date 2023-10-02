//******************************************************************************************************
// ChassisController.java
//
// Controller for Chassis.
//
//Copyright (C) 2022, PICMG.
//
//        This program is free software: you can redistribute it and/or modify
//        it under the terms of the GNU General Public License as published by
//        the Free Software Foundation, either version 3 of the License, or
//        (at your option) any later version.
//
//        This program is distributed in the hope that it will be useful,
//        but WITHOUT ANY WARRANTY; without even the implied warranty of
//        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//        GNU General Public License for more details.
//
//        You should have received a copy of the GNU General Public License
//        along with this program.  If not, see <https://www.gnu.org/licenses/>.
//*******************************************************************************************************

package org.picmg.redfish_server_template.controllers;

import org.picmg.redfish_server_template.RFmodels.AllModels.*;
import org.picmg.redfish_server_template.services.ChassisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@RestController
@RequestMapping("")
public class ChassisController {

    @Value("${async.task.retry-time}")
    Integer taskRetryTime;

    @Value("${async.task.wait-time}")
    long taskWaitTime;

    @Autowired
    ChassisService chassisService;

    @GetMapping("/redfish/v1/Chassis")
    public ResponseEntity<?> getChassisCollection() {
        String uri = "/redfish/v1/Chassis";
        Integer newTaskId = chassisService.getTaskId();
        OffsetDateTime startTime = OffsetDateTime.now();
        List<ChassisCollection> chassisCollectionList = null;
        try {
            Future<List<ChassisCollection>> resp = chassisService.getAllChassisCollection(startTime, newTaskId);
            chassisCollectionList = resp.get(taskWaitTime, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Location", chassisService.getTaskServiceURI(newTaskId.toString()));
            responseHeaders.set("Retry-After", taskRetryTime + " seconds");
            chassisService.createTaskForOperation(startTime, newTaskId, uri);
            return ResponseEntity.status(HttpStatus.ACCEPTED).headers(responseHeaders).body(chassisService.getTaskResource(newTaskId.toString()));
        } catch (Exception e)  {

        }
        return ResponseEntity.ok(chassisCollectionList);
    }

    @GetMapping("/redfish/v1/Chassis/{chassisObjectId}")
    public ResponseEntity<?> getChassis(@PathVariable String chassisObjectId, @RequestHeader String authorization) throws Exception {
        String token = authorization.substring(7);
        String uri = "/redfish/v1/Chassis/"+chassisObjectId;
        Integer newTaskId = chassisService.getTaskId();
        OffsetDateTime startTime = OffsetDateTime.now();
        List<Chassis_Chassis> chassis_chassisList = null;
        try {
            Future<List<Chassis_Chassis>> resp = chassisService.getChassis(startTime, newTaskId, chassisObjectId);
            chassis_chassisList = resp.get(taskWaitTime, TimeUnit.SECONDS);
        }catch (TimeoutException e) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Location", chassisService.getTaskServiceURI(newTaskId.toString()));
            responseHeaders.set("Retry-After", taskRetryTime + " seconds");
            chassisService.createTaskForOperation(startTime, newTaskId, uri);
            return ResponseEntity.status(HttpStatus.ACCEPTED).headers(responseHeaders).body(chassisService.getTaskResource(newTaskId.toString()));
        } catch (Exception e)  {

        }
        return ResponseEntity.ok(chassis_chassisList);
    }

    @GetMapping("/redfish/v1/Chassis/{chassisObjectId}/Controls")
    public ResponseEntity<?> getChassisControlCollection(@PathVariable String chassisObjectId, @RequestHeader String authorization) throws Exception {
        String token = authorization.substring(7);
        String uri = "/redfish/v1/Chassis/" + chassisObjectId + "/Controls";
        Integer newTaskId = chassisService.getTaskId();
        OffsetDateTime startTime = OffsetDateTime.now();

        ControlCollection controlCollection = null;
        try {
            Future<ControlCollection> resp = chassisService.getControlCollection(startTime, newTaskId, chassisObjectId);
            controlCollection = resp.get(taskWaitTime, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Location", chassisService.getTaskServiceURI(newTaskId.toString()));
            responseHeaders.set("Retry-After", taskRetryTime + " seconds");
            chassisService.createTaskForOperation(startTime, newTaskId, uri);
            return ResponseEntity.status(HttpStatus.ACCEPTED).headers(responseHeaders).body(chassisService.getTaskResource(newTaskId.toString()));
        } catch (Exception e) {

        }
        return ResponseEntity.ok(controlCollection);
    }

    @GetMapping("/redfish/v1/Chassis/{Id}/Controls/{controlObjectId}")
    public ResponseEntity<?> getPowerLimit(@PathVariable String Id, @PathVariable String controlObjectId, @RequestHeader String authorization) throws Exception {
        String token = authorization.substring(7);
        String uri = "/redfish/v1/Chassis/{Id}/Controls/" + controlObjectId;
        Integer newTaskId = chassisService.getTaskId();
        OffsetDateTime startTime = OffsetDateTime.now();
        Control_Control control = null;
        try {
            Future<Control_Control> resp = chassisService.getPowerLimit(startTime, newTaskId, Id, controlObjectId);
            control = resp.get(taskWaitTime, TimeUnit.SECONDS);
        } catch(TimeoutException e) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Location", chassisService.getTaskServiceURI(newTaskId.toString()));
            responseHeaders.set("Retry-After", taskRetryTime + " seconds");
            chassisService.createTaskForOperation(startTime, newTaskId, uri);
            return ResponseEntity.status(HttpStatus.ACCEPTED).headers(responseHeaders).body(chassisService.getTaskResource(newTaskId.toString()));
        } catch (Exception e) {

        }
        return ResponseEntity.ok(control);
    }

    @GetMapping("/redfish/v1/Chassis/{chassisObjectId}/Sensors")
    public ResponseEntity<?> getSensorCollection(@PathVariable String chassisObjectId, @RequestHeader String authorization) throws Exception {
        String token = authorization.substring(7);
        String uri = "/redfish/v1/Chassis/" + chassisObjectId + "/Sensors";
        Integer newTaskId = chassisService.getTaskId();
        OffsetDateTime startTime = OffsetDateTime.now();
        SensorCollection sensorCollection = null;
        try {
            Future<SensorCollection> resp = chassisService.getSensorCollection(startTime, newTaskId, chassisObjectId);
            sensorCollection = resp.get(taskWaitTime, TimeUnit.SECONDS);
        } catch(TimeoutException e) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Location", chassisService.getTaskServiceURI(newTaskId.toString()));
            responseHeaders.set("Retry-After", taskRetryTime + " seconds");
            chassisService.createTaskForOperation(startTime, newTaskId, uri);
            return ResponseEntity.status(HttpStatus.ACCEPTED).headers(responseHeaders).body(chassisService.getTaskResource(newTaskId.toString()));
        } catch (Exception e) {

        }

        return ResponseEntity.ok(sensorCollection);
    }

    @GetMapping("/redfish/v1/Chassis/{chassisObjectId}/Sensors/{sensorObjectId}")
    public ResponseEntity<?> getSensor(@PathVariable String chassisObjectId, @PathVariable String sensorObjectId, @RequestHeader String authorization) throws Exception {
        String token = authorization.substring(7);
        String uri = "/redfish/v1/Chassis/{chassisObjectId}/Sensors/"+sensorObjectId;
        Integer newTaskId = chassisService.getTaskId();
        OffsetDateTime startTime = OffsetDateTime.now();
        Sensor_Sensor sensor = null;
        try {
            Future<Sensor_Sensor> resp = chassisService.getSensor(startTime, newTaskId, chassisObjectId, sensorObjectId);
            sensor = resp.get(taskWaitTime, TimeUnit.SECONDS);
        } catch(TimeoutException e) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Location", chassisService.getTaskServiceURI(newTaskId.toString()));
            responseHeaders.set("Retry-After", taskRetryTime + " seconds");
            chassisService.createTaskForOperation(startTime, newTaskId, uri);
            return ResponseEntity.status(HttpStatus.ACCEPTED).headers(responseHeaders).body(chassisService.getTaskResource(newTaskId.toString()));
        } catch (Exception e) {

        }
        return ResponseEntity.ok(sensor);
    }

    @GetMapping("/redfish/v1/Chassis/{chassisObjectId}/EnvironmentMetrics")
    public ResponseEntity<?> getEnvironmentMetrics(@PathVariable String chassisObjectId, @RequestHeader String authorization) throws Exception {
        String token = authorization.substring(7);

        String uri = "redfish/v1/Chassis/"+chassisObjectId+"/EnvironmentMetrics";
        Integer newTaskId = chassisService.getTaskId();
        OffsetDateTime startTime = OffsetDateTime.now();

        EnvironmentMetrics_EnvironmentMetrics obj = null;
        try {
            Future<EnvironmentMetrics_EnvironmentMetrics> resp = chassisService.getEnvironmentMetrics(startTime, newTaskId, chassisObjectId);
            obj = resp.get(taskWaitTime, TimeUnit.SECONDS);
        } catch(TimeoutException e) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Location", chassisService.getTaskServiceURI(newTaskId.toString()));
            responseHeaders.set("Retry-After", taskRetryTime + " seconds");
            chassisService.createTaskForOperation(startTime, newTaskId, uri);
            return ResponseEntity.status(HttpStatus.ACCEPTED).headers(responseHeaders).body(chassisService.getTaskResource(newTaskId.toString()));
        } catch (Exception e) {

        }

        return ResponseEntity.ok(obj);
    }

    @GetMapping("/redfish/v1/Chassis/{chassisObjectId}/Power")
    public ResponseEntity<?> getPower(@PathVariable String chassisObjectId, @RequestHeader String authorization) throws Exception {
        String token = authorization.substring(7);

        String uri = "/redfish/v1/Chassis/"+chassisObjectId+"/Power";
        Integer newTaskId = chassisService.getTaskId();
        OffsetDateTime startTime = OffsetDateTime.now();

        Power_Power obj = null;
        try {
            Future<Power_Power> resp = chassisService.getPower(startTime, newTaskId, chassisObjectId);
            obj = resp.get(taskWaitTime, TimeUnit.SECONDS);
        } catch(TimeoutException e) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Location", chassisService.getTaskServiceURI(newTaskId.toString()));
            responseHeaders.set("Retry-After", taskRetryTime + " seconds");
            chassisService.createTaskForOperation(startTime, newTaskId, uri);
            return ResponseEntity.status(HttpStatus.ACCEPTED).headers(responseHeaders).body(chassisService.getTaskResource(newTaskId.toString()));
        } catch (Exception e) {

        }
        return ResponseEntity.ok(obj);
    }

    @GetMapping("/redfish/v1/Chassis/{chassisObjectId}/PowerSubsystem")
    public ResponseEntity<?> getPowerSubSystem(@PathVariable String chassisObjectId, @RequestHeader String authorization) throws Exception {
        String token = authorization.substring(7);

        String uri = "/redfish/v1/Chassis/"+chassisObjectId+"/PowerSubsystem";
        Integer newTaskId = chassisService.getTaskId();
        OffsetDateTime startTime = OffsetDateTime.now();

        PowerSubsystem_PowerSubsystem obj = null;
        try {
            Future<PowerSubsystem_PowerSubsystem> resp = chassisService.getPowerSubsystem(startTime, newTaskId, chassisObjectId);
            obj = resp.get(taskWaitTime, TimeUnit.SECONDS);
        } catch(TimeoutException e) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Location", chassisService.getTaskServiceURI(newTaskId.toString()));
            responseHeaders.set("Retry-After", taskRetryTime + " seconds");
            chassisService.createTaskForOperation(startTime, newTaskId, uri);
            return ResponseEntity.status(HttpStatus.ACCEPTED).headers(responseHeaders).body(chassisService.getTaskResource(newTaskId.toString()));
        } catch (Exception e) {

        }

        return ResponseEntity.ok(obj);
    }

    @GetMapping("/redfish/v1/Chassis/{chassisObjectId}/PowerSubsystem/PowerSupplies")
    public ResponseEntity<?> getPowerSubSystemPowerSupplyCollection(@PathVariable String chassisObjectId, @RequestHeader String authorization) throws Exception {
        String token = authorization.substring(7);

        String uri = "/redfish/v1/Chassis/"+chassisObjectId+"/PowerSubsystem/PowerSupplies";
        Integer newTaskId = chassisService.getTaskId();
        OffsetDateTime startTime = OffsetDateTime.now();

        PowerSupplyCollection obj = null;
        try {
            Future<PowerSupplyCollection> resp = chassisService.getPowerSupplyCollection(startTime, newTaskId, chassisObjectId);
            obj = resp.get(taskWaitTime, TimeUnit.SECONDS);
        } catch(TimeoutException e) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Location", chassisService.getTaskServiceURI(newTaskId.toString()));
            responseHeaders.set("Retry-After", taskRetryTime + " seconds");
            chassisService.createTaskForOperation(startTime, newTaskId, uri);
            return ResponseEntity.status(HttpStatus.ACCEPTED).headers(responseHeaders).body(chassisService.getTaskResource(newTaskId.toString()));
        } catch (Exception e) {

        }
        return ResponseEntity.ok(obj);
    }

    @GetMapping("/redfish/v1/Chassis/{chassisObjectId}/PowerSubsystem/Batteries")
    public ResponseEntity<?> getBatteryCollection(@PathVariable String chassisObjectId, @RequestHeader String authorization) throws Exception {
        String token = authorization.substring(7);

        String uri = "/redfish/v1/Chassis/"+chassisObjectId+"/PowerSubsystem/Batteries";
        Integer newTaskId = chassisService.getTaskId();
        OffsetDateTime startTime = OffsetDateTime.now();

        BatteryCollection obj = null;
        try {
            Future<BatteryCollection> resp = chassisService.getBatteryCollection(startTime, newTaskId, chassisObjectId);
            obj = resp.get(taskWaitTime, TimeUnit.SECONDS);
        } catch(TimeoutException e) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Location", chassisService.getTaskServiceURI(newTaskId.toString()));
            responseHeaders.set("Retry-After", taskRetryTime + " seconds");
            chassisService.createTaskForOperation(startTime, newTaskId, uri);
            return ResponseEntity.status(HttpStatus.ACCEPTED).headers(responseHeaders).body(chassisService.getTaskResource(newTaskId.toString()));
        } catch (Exception e) {

        }
        return ResponseEntity.ok(obj);
    }

    @GetMapping("/redfish/v1/Chassis/{chassisObjectId}/PowerSubsystem/PowerSupplies/{powerSupplyObjectId}")
    public ResponseEntity<?> getPowerSupplyObject(@PathVariable String chassisObjectId, @PathVariable String powerSupplyObjectId, @RequestHeader String authorization) throws Exception {
        String token = authorization.substring(7);

        String uri = "/redfish/v1/Chassis/"+chassisObjectId+"/PowerSubsystem/PowerSupplies/"+powerSupplyObjectId+"";
        Integer newTaskId = chassisService.getTaskId();
        OffsetDateTime startTime = OffsetDateTime.now();

        PowerSupply_PowerSupply obj = null;
        try {
            Future<PowerSupply_PowerSupply> resp = chassisService.getPowerSupply(startTime, newTaskId, chassisObjectId, powerSupplyObjectId);
            obj = resp.get(taskWaitTime, TimeUnit.SECONDS);
        } catch(TimeoutException e) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Location", chassisService.getTaskServiceURI(newTaskId.toString()));
            responseHeaders.set("Retry-After", taskRetryTime + " seconds");
            chassisService.createTaskForOperation(startTime, newTaskId, uri);
            return ResponseEntity.status(HttpStatus.ACCEPTED).headers(responseHeaders).body(chassisService.getTaskResource(newTaskId.toString()));
        } catch (Exception e) {

        }
        return ResponseEntity.ok(obj);
    }

    @GetMapping("/redfish/v1/Chassis/{chassisObjectId}/PowerSubsystem/PowerSupplies/{powerSupplyObjectId}/Assembly")
    public ResponseEntity<?> getAssembly(@PathVariable String chassisObjectId, @PathVariable String powerSupplyObjectId, @RequestHeader String authorization) throws Exception {
        String token = authorization.substring(7);

        String uri = "/redfish/v1/Chassis/"+chassisObjectId+"/PowerSubsystem/PowerSupplies/"+powerSupplyObjectId+"/Assembly";
        Integer newTaskId = chassisService.getTaskId();
        OffsetDateTime startTime = OffsetDateTime.now();

        Assembly_Assembly obj = null;
        try {
            Future<Assembly_Assembly> resp = chassisService.getAssembly(startTime, newTaskId, chassisObjectId, powerSupplyObjectId);
            obj = resp.get(taskWaitTime, TimeUnit.SECONDS);
        } catch(TimeoutException e) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Location", chassisService.getTaskServiceURI(newTaskId.toString()));
            responseHeaders.set("Retry-After", taskRetryTime + " seconds");
            chassisService.createTaskForOperation(startTime, newTaskId, uri);
            return ResponseEntity.status(HttpStatus.ACCEPTED).headers(responseHeaders).body(chassisService.getTaskResource(newTaskId.toString()));
        } catch (Exception e) {

        }
        return ResponseEntity.ok(obj);
    }

    @GetMapping("/redfish/v1/Chassis/{chassisObjectId}/PowerSubsystem/PowerSupplies/{powerSupplyObjectId}/Metrics")
    public ResponseEntity<?> getPowerSupplyMetrics(@PathVariable String chassisObjectId, @PathVariable String powerSupplyObjectId, @RequestHeader String authorization) throws Exception {
        String token = authorization.substring(7);

        String uri = "/redfish/v1/Chassis/"+chassisObjectId+"/PowerSubsystem/PowerSupplies/"+powerSupplyObjectId+"/Metrics";
        Integer newTaskId = chassisService.getTaskId();
        OffsetDateTime startTime = OffsetDateTime.now();

        PowerSupplyMetrics_PowerSupplyMetrics obj = null;
        try {
            Future<PowerSupplyMetrics_PowerSupplyMetrics> resp = chassisService.getPowerSupplyMetrics(startTime, newTaskId, chassisObjectId, powerSupplyObjectId);
            obj = resp.get(taskWaitTime, TimeUnit.SECONDS);
        } catch(TimeoutException e) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Location", chassisService.getTaskServiceURI(newTaskId.toString()));
            responseHeaders.set("Retry-After", taskRetryTime + " seconds");
            chassisService.createTaskForOperation(startTime, newTaskId, uri);
            return ResponseEntity.status(HttpStatus.ACCEPTED).headers(responseHeaders).body(chassisService.getTaskResource(newTaskId.toString()));
        } catch (Exception e) {

        }
        return ResponseEntity.ok(obj);
    }

    @GetMapping("/redfish/v1/Chassis/{chassisObjectId}/ThermalSubsystem")
    public ResponseEntity<?> getThermalSubsystem(@PathVariable String chassisObjectId, @RequestHeader String authorization) throws Exception {
        String token = authorization.substring(7);

        String uri = "/redfish/v1/Chassis/"+chassisObjectId+"/ThermalSubsystem";
        Integer newTaskId = chassisService.getTaskId();
        OffsetDateTime startTime = OffsetDateTime.now();

        ThermalSubsystem_ThermalSubsystem obj = null;
        try {
            Future<ThermalSubsystem_ThermalSubsystem> resp = chassisService.getThermalSubsystem(startTime, newTaskId, chassisObjectId);
            obj = resp.get(taskWaitTime, TimeUnit.SECONDS);
        } catch(TimeoutException e) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Location", chassisService.getTaskServiceURI(newTaskId.toString()));
            responseHeaders.set("Retry-After", taskRetryTime + " seconds");
            chassisService.createTaskForOperation(startTime, newTaskId, uri);
            return ResponseEntity.status(HttpStatus.ACCEPTED).headers(responseHeaders).body(chassisService.getTaskResource(newTaskId.toString()));
        } catch (Exception e) {

        }
        return ResponseEntity.ok(obj);
    }

    @GetMapping("/redfish/v1/Chassis/{chassisObjectId}/ThermalSubsystem/ThermalMetrics")
    public ResponseEntity<?> getThermalMetrics(@PathVariable String chassisObjectId, @RequestHeader String authorization) throws Exception {
        String token = authorization.substring(7);

        String uri = "/redfish/v1/Chassis/"+chassisObjectId+"/ThermalSubsystem/ThermalMetrics";
        Integer newTaskId = chassisService.getTaskId();
        OffsetDateTime startTime = OffsetDateTime.now();

        ThermalMetrics_ThermalMetrics obj = null;
        try {
            Future<ThermalMetrics_ThermalMetrics> resp = chassisService.getThermalMetrics(startTime, newTaskId, chassisObjectId);
            obj = resp.get(taskWaitTime, TimeUnit.SECONDS);
        } catch(TimeoutException e) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Location", chassisService.getTaskServiceURI(newTaskId.toString()));
            responseHeaders.set("Retry-After", taskRetryTime + " seconds");
            chassisService.createTaskForOperation(startTime, newTaskId, uri);
            return ResponseEntity.status(HttpStatus.ACCEPTED).headers(responseHeaders).body(chassisService.getTaskResource(newTaskId.toString()));
        } catch (Exception e) {

        }
        return ResponseEntity.ok(obj);
    }

    @GetMapping("/redfish/v1/Chassis/{chassisObjectId}/ThermalSubsystem/Fans")
    public ResponseEntity<?> getFanCollection(@PathVariable String chassisObjectId, @RequestHeader String authorization) throws Exception {
        String token = authorization.substring(7);

        String uri = "/redfish/v1/Chassis/"+chassisObjectId+"/ThermalSubsystem/Fans";
        Integer newTaskId = chassisService.getTaskId();
        OffsetDateTime startTime = OffsetDateTime.now();

        FanCollection obj = null;
        try {
            Future<FanCollection> resp = chassisService.getFanCollection(startTime, newTaskId, chassisObjectId);
            obj = resp.get(taskWaitTime, TimeUnit.SECONDS);
        } catch(TimeoutException e) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Location", chassisService.getTaskServiceURI(newTaskId.toString()));
            responseHeaders.set("Retry-After", taskRetryTime + " seconds");
            chassisService.createTaskForOperation(startTime, newTaskId, uri);
            return ResponseEntity.status(HttpStatus.ACCEPTED).headers(responseHeaders).body(chassisService.getTaskResource(newTaskId.toString()));
        } catch (Exception e) {

        }
        return ResponseEntity.ok(obj);
    }

    @GetMapping("/redfish/v1/Chassis/{chassisObjectId}/ThermalSubsystem/Fans/{fanObjectId}")
    public ResponseEntity<?> getFan(@PathVariable String chassisObjectId, @PathVariable String fanObjectId, @RequestHeader String authorization) throws Exception {
        String token = authorization.substring(7);

        String uri = "/redfish/v1/Chassis/"+chassisObjectId+"/ThermalSubsystem/Fans/"+fanObjectId+"";
        Integer newTaskId = chassisService.getTaskId();
        OffsetDateTime startTime = OffsetDateTime.now();

        Fan_Fan obj = null;
        try {
            Future<Fan_Fan> resp = chassisService.getFan(startTime, newTaskId, chassisObjectId, fanObjectId);
            obj = resp.get(taskWaitTime, TimeUnit.SECONDS);
        } catch(TimeoutException e) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Location", chassisService.getTaskServiceURI(newTaskId.toString()));
            responseHeaders.set("Retry-After", taskRetryTime + " seconds");
            chassisService.createTaskForOperation(startTime, newTaskId, uri);
            return ResponseEntity.status(HttpStatus.ACCEPTED).headers(responseHeaders).body(chassisService.getTaskResource(newTaskId.toString()));
        } catch (Exception e) {

        }
        return ResponseEntity.ok(obj);
    }

    @GetMapping("/redfish/v1/Chassis/{chassisObjectId}/PowerSubsystem/Batteries/{batteryObjectId}")
    public ResponseEntity<?> getBattery(@PathVariable String chassisObjectId, @PathVariable String batteryObjectId, @RequestHeader String authorization) throws Exception {
        String token = authorization.substring(7);

        String uri = "/redfish/v1/Chassis/"+chassisObjectId+"/PowerSubsystem/Batteries/"+batteryObjectId+"";
        Integer newTaskId = chassisService.getTaskId();
        OffsetDateTime startTime = OffsetDateTime.now();

        Battery_Battery obj = null;
        try {
            Future<Battery_Battery> resp = chassisService.getBattery(startTime, newTaskId, chassisObjectId, batteryObjectId);
            obj = resp.get(taskWaitTime, TimeUnit.SECONDS);
        } catch(TimeoutException e) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Location", chassisService.getTaskServiceURI(newTaskId.toString()));
            responseHeaders.set("Retry-After", taskRetryTime + " seconds");
            chassisService.createTaskForOperation(startTime, newTaskId, uri);
            return ResponseEntity.status(HttpStatus.ACCEPTED).headers(responseHeaders).body(chassisService.getTaskResource(newTaskId.toString()));
        } catch (Exception e) {

        }
        return ResponseEntity.ok(obj);
    }

    @GetMapping("/redfish/v1/Chassis/{chassisObjectId}/PowerSubsystem/Batteries/{batteryObjectId}/Metrics")
    public ResponseEntity<?> getBatteryMetrics(@PathVariable String chassisObjectId, @PathVariable String batteryObjectId, @RequestHeader String authorization) throws Exception {
        String token = authorization.substring(7);

        String uri = "/redfish/v1/Chassis/"+chassisObjectId+"/PowerSubsystem/Batteries/"+batteryObjectId+"/Metrics";
        Integer newTaskId = chassisService.getTaskId();
        OffsetDateTime startTime = OffsetDateTime.now();

        BatteryMetrics_BatteryMetrics obj = null;
        try {
            Future<BatteryMetrics_BatteryMetrics> resp = chassisService.getBatteryMetrics(startTime, newTaskId, chassisObjectId, batteryObjectId);
            obj = resp.get(taskWaitTime, TimeUnit.SECONDS);
        } catch(TimeoutException e) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Location", chassisService.getTaskServiceURI(newTaskId.toString()));
            responseHeaders.set("Retry-After", taskRetryTime + " seconds");
            chassisService.createTaskForOperation(startTime, newTaskId, uri);
            return ResponseEntity.status(HttpStatus.ACCEPTED).headers(responseHeaders).body(chassisService.getTaskResource(newTaskId.toString()));
        } catch (Exception e) {

        }
        return ResponseEntity.ok(obj);
    }


    @GetMapping("/redfish/v1/Chassis/{chassisObjectId}/TrustedComponents")
    public ResponseEntity<?> getTrustedComponentCollection(@PathVariable String  chassisObjectId, @RequestHeader String authorization) throws Exception {
        String token = authorization.substring(7);

        String uri = "/redfish/v1/Chassis/"+chassisObjectId+"/TrustedComponents";
        Integer newTaskId = chassisService.getTaskId();
        OffsetDateTime startTime = OffsetDateTime.now();

        TrustedComponentCollection obj = null;
        try {
            Future<TrustedComponentCollection> resp = chassisService.getTrustedComponentCollection(startTime, newTaskId, chassisObjectId);
            obj = resp.get(taskWaitTime, TimeUnit.SECONDS);
        } catch(TimeoutException e) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Location", chassisService.getTaskServiceURI(newTaskId.toString()));
            responseHeaders.set("Retry-After", taskRetryTime + " seconds");
            chassisService.createTaskForOperation(startTime, newTaskId, uri);
            return ResponseEntity.status(HttpStatus.ACCEPTED).headers(responseHeaders).body(chassisService.getTaskResource(newTaskId.toString()));
        } catch (Exception e) {

        }
        return ResponseEntity.ok(obj);
    }

    @GetMapping("/redfish/v1/Chassis/{chassisObjectId}/TrustedComponents/{trustedComponentObjectId}/Certificates")
    public ResponseEntity<?> getCertificatesCollection(@PathVariable String chassisObjectId, @PathVariable String trustedComponentObjectId, @RequestHeader String authorization) throws Exception {
        String token = authorization.substring(7);

        String uri = "/redfish/v1/Chassis/"+chassisObjectId+"/TrustedComponents/"+trustedComponentObjectId+"/Certificates";
        Integer newTaskId = chassisService.getTaskId();
        OffsetDateTime startTime = OffsetDateTime.now();

        CertificateCollection obj = null;
        try {
            Future<CertificateCollection> resp = chassisService.getCertificateCollection(startTime, newTaskId, chassisObjectId, trustedComponentObjectId);
            obj = resp.get(taskWaitTime, TimeUnit.SECONDS);
        } catch(TimeoutException e) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Location", chassisService.getTaskServiceURI(newTaskId.toString()));
            responseHeaders.set("Retry-After", taskRetryTime + " seconds");
            chassisService.createTaskForOperation(startTime, newTaskId, uri);
            return ResponseEntity.status(HttpStatus.ACCEPTED).headers(responseHeaders).body(chassisService.getTaskResource(newTaskId.toString()));
        } catch (Exception e) {

        }
        return ResponseEntity.ok(obj);
    }

    @GetMapping("/redfish/v1/Chassis/{chassisObjectId}/TrustedComponents/{trustedComponentObjectId}/Certificates/{certificateObjectId}")
    public ResponseEntity<?> getCertificate(@PathVariable String chassisObjectId, @PathVariable String trustedComponentObjectId, @PathVariable String certificateObjectId, @RequestHeader String authorization) throws Exception {
        String token = authorization.substring(7);

        String uri = "/redfish/v1/Chassis/"+chassisObjectId+"/TrustedComponents/"+trustedComponentObjectId+"/Certificates/"+certificateObjectId+"";
        Integer newTaskId = chassisService.getTaskId();
        OffsetDateTime startTime = OffsetDateTime.now();

        Certificate_Certificate obj = null;
        try {
            Future<Certificate_Certificate> resp = chassisService.getCertificate(startTime, newTaskId, chassisObjectId, trustedComponentObjectId, certificateObjectId);
            obj = resp.get(taskWaitTime, TimeUnit.SECONDS);
        } catch(TimeoutException e) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Location", chassisService.getTaskServiceURI(newTaskId.toString()));
            responseHeaders.set("Retry-After", taskRetryTime + " seconds");
            chassisService.createTaskForOperation(startTime, newTaskId, uri);
            return ResponseEntity.status(HttpStatus.ACCEPTED).headers(responseHeaders).body(chassisService.getTaskResource(newTaskId.toString()));
        } catch (Exception e) {

        }
        return ResponseEntity.ok(obj);
    }

    @GetMapping("/redfish/v1/Chassis/{chassisObjectId}/TrustedComponents/{trustedComponentObjectId}")
    public ResponseEntity<?> getTrustedComponent(@PathVariable String chassisObjectId, @PathVariable String trustedComponentObjectId, @RequestHeader String authorization) throws Exception {
        String token = authorization.substring(7);

        String uri = "/redfish/v1/Chassis/"+chassisObjectId+"/TrustedComponents/"+trustedComponentObjectId+"";
        Integer newTaskId = chassisService.getTaskId();
        OffsetDateTime startTime = OffsetDateTime.now();

        TrustedComponent_TrustedComponent obj = null;
        try {
            Future<TrustedComponent_TrustedComponent> resp = chassisService.getTrustedComponent(startTime, newTaskId, chassisObjectId, trustedComponentObjectId);
            obj = resp.get(taskWaitTime, TimeUnit.SECONDS);
        } catch(TimeoutException e) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Location", chassisService.getTaskServiceURI(newTaskId.toString()));
            responseHeaders.set("Retry-After", taskRetryTime + " seconds");
            chassisService.createTaskForOperation(startTime, newTaskId, uri);
            return ResponseEntity.status(HttpStatus.ACCEPTED).headers(responseHeaders).body(chassisService.getTaskResource(newTaskId.toString()));
        } catch (Exception e) {

        }
        return ResponseEntity.ok(obj);
    }

}
