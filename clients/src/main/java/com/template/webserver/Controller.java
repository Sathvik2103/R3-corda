package com.template.webserver;

import com.onboarding.user.KYCVerificationFlow;
import com.onboarding.user.states.*;
import net.corda.core.contracts.*;
import net.corda.core.crypto.SecureHash;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.node.NodeInfo;
import net.corda.core.transactions.SignedTransaction;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import com.onboarding.user.ApplicantInformationCollectionFlow;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Define your API endpoints here.
 */
@RestController
@RequestMapping("/") // The paths for HTTP requests are relative to this base path.
public class Controller {
    private static final String NOTARY_X500 = "O=Notary,L=London,C=GB";
    private static final String OTHER_PARTY_X500 = "O=Applicant-B,L=Mumbai,C=IN";
    private final CordaRPCOps proxy;
    private final CordaX500Name me;

    public Controller(NodeRPCConnection rpc) {
        this.proxy = rpc.proxy;
        this.me = proxy.nodeInfo().getLegalIdentities().get(0).getName();
    }

    /** Helpers for filtering the network map cache. */
    public String toDisplayString(X500Name name){
        return BCStyle.INSTANCE.toString(name);
    }

    private boolean isNotary(NodeInfo nodeInfo) {
        return !proxy.notaryIdentities()
                .stream().filter(el -> nodeInfo.isLegalIdentity(el))
                .collect(Collectors.toList()).isEmpty();
    }

    private boolean isMe(NodeInfo nodeInfo){
        return nodeInfo.getLegalIdentities().get(0).getName().equals(me);
    }

    private boolean isNetworkMap(NodeInfo nodeInfo){
        return nodeInfo.getLegalIdentities().get(0).getName().getOrganisation().equals("Network Map Service");
    }

    @GetMapping(value = "/status", produces = TEXT_PLAIN_VALUE)
    private String status() {
        return "200";
    }

    @GetMapping(value = "/servertime", produces = TEXT_PLAIN_VALUE)
    private String serverTime() {
        return (LocalDateTime.ofInstant(proxy.currentNodeTime(), ZoneId.of("UTC"))).toString();
    }

    @GetMapping(value = "/addresses", produces = TEXT_PLAIN_VALUE)
    private String addresses() {
        return proxy.nodeInfo().getAddresses().toString();
    }

    @GetMapping(value = "/identities", produces = TEXT_PLAIN_VALUE)
    private String identities() {
        return proxy.nodeInfo().getLegalIdentities().toString();
    }

    @GetMapping(value = "/platformversion", produces = TEXT_PLAIN_VALUE)
    private String platformVersion() {
        return Integer.toString(proxy.nodeInfo().getPlatformVersion());
    }

    @GetMapping(value = "/peers", produces = APPLICATION_JSON_VALUE)
    public HashMap<String, List<String>> getPeers() {
        HashMap<String, List<String>> myMap = new HashMap<>();

        // Find all nodes that are not notaries, ourself, or the network map.
        Stream<NodeInfo> filteredNodes = proxy.networkMapSnapshot().stream()
                .filter(el -> !isNotary(el) && !isMe(el) && !isNetworkMap(el));
        // Get their names as strings
        List<String> nodeNames = filteredNodes.map(el -> el.getLegalIdentities().get(0).getName().toString())
                .collect(Collectors.toList());

        myMap.put("peers", nodeNames);
        return myMap;
    }

    @GetMapping(value = "/notaries", produces = TEXT_PLAIN_VALUE)
    private String notaries() {
        return proxy.notaryIdentities().toString();
    }

    @GetMapping(value = "/flows", produces = TEXT_PLAIN_VALUE)
    private String flows() {
        return proxy.registeredFlows().toString();
    }

    @GetMapping(value = "/states", produces = TEXT_PLAIN_VALUE)
    private String states() {
        return proxy.vaultQuery(ContractState.class).getStates().toString();
    }

    @GetMapping(value = "/me",produces = APPLICATION_JSON_VALUE)
    private HashMap<String, String> whoami(){
        HashMap<String, String> myMap = new HashMap<>();
        myMap.put("me", me.toString());
        return myMap;
    }

    @GetMapping(value = "/ApplicantDetails/{applicantId}",produces = APPLICATION_JSON_VALUE)
    public List<StateAndRef<ApplicantDetailsState>> ApplicantDetails(@PathVariable UniqueIdentifier applicantId) {

        return proxy.vaultQuery(ApplicantDetailsState.class).getStates().stream().filter(el -> el.getState().getData().getLinearId().equals(applicantId)).collect(Collectors.toList());
    }

    @GetMapping(value = "/CompanyDirectors/{applicantId}",produces = APPLICATION_JSON_VALUE)
    public List<StateAndRef<CompanyDirectorDetailsState>> CompanyDirectors(@PathVariable UniqueIdentifier applicantId) {

        return proxy.vaultQuery(CompanyDirectorDetailsState.class).getStates().stream().filter(el -> el.getState().getData().getLinearId().equals(applicantId)).collect(Collectors.toList());
    }

    @GetMapping(value = "/CompanyProfiles/{applicantId}",produces = APPLICATION_JSON_VALUE)
    public List<StateAndRef<CompanyProfileState>> CompanyProfiles(@PathVariable UniqueIdentifier applicantId) {

        return proxy.vaultQuery(CompanyProfileState.class).getStates().stream().filter(el -> el.getState().getData().getLinearId().equals(applicantId)).collect(Collectors.toList());
    }

    @PostMapping(value = "/receive-partial-application",
            produces = APPLICATION_JSON_VALUE,
            consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> onboardApplicant(@RequestBody String jsonData) {
        try {
            // Resolve Notary and Other Party
            CordaX500Name notaryX500Name = CordaX500Name.parse(NOTARY_X500);
            Party notary = proxy.wellKnownPartyFromX500Name(notaryX500Name);

            CordaX500Name otherPartyX500Name = CordaX500Name.parse(OTHER_PARTY_X500);
            Party otherParty = proxy.wellKnownPartyFromX500Name(otherPartyX500Name);

            // Start the Initiator flow with the provided parameters
            SignedTransaction result = proxy.startTrackedFlowDynamic(
                    ApplicantInformationCollectionFlow.Initiator.class, jsonData, otherParty
            ).getReturnValue().get();

            // Return a success response with the transaction details
            Map<String, Object> response = new HashMap<>();
            response.put("Message", "Transaction committed to ledger");
            response.put("TransactionId", result.getId()+"");
            response.put("ApplicantId", result.getTx().outputsOfType(ApplicantDetailsState.class).get(0).getLinearId().getId());

            // Return the response as JSON
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(response);
        } catch (Exception e) {
            // Handle exceptions and return an appropriate error response
            Map<String, Object> response = new HashMap<>();
            response.put("Status", "error");
            response.put("Message", "Error occurred: " + e.getMessage());
            response.put("InputData", jsonData);

            // Return the response as a structured JSON object
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
        }
    }

    @PostMapping(value = "/onboard-kyc",
            produces = APPLICATION_JSON_VALUE,
            consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> onboardKyc(@RequestBody String jsonData) {
        try {
            // Resolve Notary and Other Party
            CordaX500Name notaryX500Name = CordaX500Name.parse(NOTARY_X500);
            Party notary = proxy.wellKnownPartyFromX500Name(notaryX500Name);

            CordaX500Name otherPartyX500Name = CordaX500Name.parse(OTHER_PARTY_X500);
            Party otherParty = proxy.wellKnownPartyFromX500Name(otherPartyX500Name);


            // Start the Initiator flow with the provided parameters
            SignedTransaction result = proxy.startTrackedFlowDynamic(
                    KYCVerificationFlow.Initiator.class, jsonData, otherParty
            ).getReturnValue().get();

            // Return a success response with the transaction details
            Map<String, Object> response = new HashMap<>();
            response.put("Message", "KYC Verification Completed And Transaction committed to ledger.");
            response.put("TransactionId",result.getId()+"");
            response.put("ApplicantId", result.getTx().outputsOfType(ApplicantBankState.class).get(0).getLinearId().getId());
            response.put("Response Code", result.getTx().outputsOfType(ApplicantAadhaarState.class).get(0).getApplicantAadharVerificationId());

            // Return the response as JSON
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(response);
        } catch (Exception e) {
            // Handle exceptions and return an appropriate error response
            Map<String, Object> response = new HashMap<>();
            response.put("Status", "error");
            response.put("Message", "Error occurred: " + e.getMessage());
            response.put("InputData", jsonData);

            // Return the response as a structured JSON object
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
        }
    }
}