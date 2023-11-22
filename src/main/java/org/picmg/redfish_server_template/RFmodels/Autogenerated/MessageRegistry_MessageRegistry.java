package org.picmg.redfish_server_template.RFmodels.Autogenerated;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.picmg.redfish_server_template.RFmodels.custom.RedfishObject;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * The MessageRegistry schema describes all message registries.  It represents the properties for the message registries themselves.
 */
@ApiModel(description = "The MessageRegistry schema describes all message registries.  It represents the properties for the message registries themselves.")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-10-18T12:30:08.978277474-07:00[America/Phoenix]")

@Document("MessageRegistry")
public class MessageRegistry_MessageRegistry   {
  @Field("_id")
  @Id
  private ObjectId _id;

  @JsonProperty("@odata.type")
  @Field("@odata.type")
  private String atOdataType;

  @JsonProperty("Actions")
  @Field("Actions")
  private RedfishObject actions;

  @JsonProperty("Description")
  @Field("Description")
  private String description;

  @JsonProperty("Id")
  @Field("Id")
  private String id;

  @JsonProperty("Language")
  @Field("Language")
  private String language;

  @JsonProperty("Messages")
  @Field("Messages")
  private Object messages;

  @JsonProperty("Name")
  @Field("Name")
  private String name;

  @JsonProperty("Oem")
  @Field("Oem")
  @Valid
  private Map<String, Object> oem = null;

  @JsonProperty("OwningEntity")
  @Field("OwningEntity")
  private String owningEntity;

  @JsonProperty("RegistryPrefix")
  @Field("RegistryPrefix")
  private String registryPrefix;

  @JsonProperty("RegistryVersion")
  @Field("RegistryVersion")
  private String registryVersion;

  public MessageRegistry_MessageRegistry atOdataType(String atOdataType) {
    this.atOdataType = atOdataType;
    return this;
  }

  /**
   * The type of a resource.
   * @return atOdataType
  */
  @ApiModelProperty(required = true, readOnly = true, value = "The type of a resource.")
  @NotNull


  public String getAtOdataType() {
    return atOdataType;
  }

  public void setAtOdataType(String atOdataType) {
    this.atOdataType = atOdataType;
  }

  public MessageRegistry_MessageRegistry actions(RedfishObject actions) {
    this.actions = actions;
    return this;
  }

  /**
   * Get actions
   * @return actions
  */
  @ApiModelProperty(value = "")

  @Valid

  public RedfishObject getActions() {
    return actions;
  }

  public void setActions(RedfishObject actions) {
    this.actions = actions;
  }

  public MessageRegistry_MessageRegistry description(String description) {
    this.description = description;
    return this;
  }

  /**
   * The description of this resource.  Used for commonality in the schema definitions.
   * @return description
  */
  @ApiModelProperty(value = "The description of this resource.  Used for commonality in the schema definitions.")


  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public MessageRegistry_MessageRegistry id(String id) {
    this.id = id;
    return this;
  }

  /**
   * The unique identifier for this resource within the collection of similar resources.
   * @return id
  */
  @ApiModelProperty(required = true, value = "The unique identifier for this resource within the collection of similar resources.")
  @NotNull


  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public MessageRegistry_MessageRegistry language(String language) {
    this.language = language;
    return this;
  }

  /**
   * The RFC5646-conformant language code for the message registry.
   * @return language
  */
  @ApiModelProperty(required = true, readOnly = true, value = "The RFC5646-conformant language code for the message registry.")
  @NotNull


  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public MessageRegistry_MessageRegistry messages(Object messages) {
    this.messages = messages;
    return this;
  }

  /**
   * The message keys contained in the message registry.
   * @return messages
  */
  @ApiModelProperty(required = true, value = "The message keys contained in the message registry.")
  @NotNull

  @Valid

  public Object getMessages() {
    return messages;
  }

  public void setMessages(Object messages) {
    this.messages = messages;
  }

  public MessageRegistry_MessageRegistry name(String name) {
    this.name = name;
    return this;
  }

  /**
   * The name of the resource or array member.
   * @return name
  */
  @ApiModelProperty(required = true, value = "The name of the resource or array member.")
  @NotNull


  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public MessageRegistry_MessageRegistry oem(Map<String, Object> oem) {
    this.oem = oem;
    return this;
  }

  public MessageRegistry_MessageRegistry putOemItem(String key, Object oemItem) {
    if (this.oem == null) {
      this.oem = new HashMap<>();
    }
    this.oem.put(key, oemItem);
    return this;
  }

  /**
   * The OEM extension.
   * @return oem
  */
  @ApiModelProperty(value = "The OEM extension.")


  public Map<String, Object> getOem() {
    return oem;
  }

  public void setOem(Map<String, Object> oem) {
    this.oem = oem;
  }

  public MessageRegistry_MessageRegistry owningEntity(String owningEntity) {
    this.owningEntity = owningEntity;
    return this;
  }

  /**
   * The organization or company that publishes this message registry.
   * @return owningEntity
  */
  @ApiModelProperty(required = true, readOnly = true, value = "The organization or company that publishes this message registry.")
  @NotNull


  public String getOwningEntity() {
    return owningEntity;
  }

  public void setOwningEntity(String owningEntity) {
    this.owningEntity = owningEntity;
  }

  public MessageRegistry_MessageRegistry registryPrefix(String registryPrefix) {
    this.registryPrefix = registryPrefix;
    return this;
  }

  /**
   * The single-word prefix that is used in forming and decoding MessageIds.
   * @return registryPrefix
  */
  @ApiModelProperty(required = true, readOnly = true, value = "The single-word prefix that is used in forming and decoding MessageIds.")
  @NotNull


  public String getRegistryPrefix() {
    return registryPrefix;
  }

  public void setRegistryPrefix(String registryPrefix) {
    this.registryPrefix = registryPrefix;
  }

  public MessageRegistry_MessageRegistry registryVersion(String registryVersion) {
    this.registryVersion = registryVersion;
    return this;
  }

  /**
   * The message registry version in the middle portion of a MessageId.
   * @return registryVersion
  */
  @ApiModelProperty(required = true, readOnly = true, value = "The message registry version in the middle portion of a MessageId.")
  @NotNull

@Pattern(regexp="^\\\\d+\\\\.\\\\d+\\\\.\\\\d+$") 
  public String getRegistryVersion() {
    return registryVersion;
  }

  public void setRegistryVersion(String registryVersion) {
    this.registryVersion = registryVersion;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MessageRegistry_MessageRegistry messageRegistry_MessageRegistry = (MessageRegistry_MessageRegistry) o;
    return Objects.equals(this.atOdataType, messageRegistry_MessageRegistry.atOdataType) &&
        Objects.equals(this.actions, messageRegistry_MessageRegistry.actions) &&
        Objects.equals(this.description, messageRegistry_MessageRegistry.description) &&
        Objects.equals(this.id, messageRegistry_MessageRegistry.id) &&
        Objects.equals(this.language, messageRegistry_MessageRegistry.language) &&
        Objects.equals(this.messages, messageRegistry_MessageRegistry.messages) &&
        Objects.equals(this.name, messageRegistry_MessageRegistry.name) &&
        Objects.equals(this.oem, messageRegistry_MessageRegistry.oem) &&
        Objects.equals(this.owningEntity, messageRegistry_MessageRegistry.owningEntity) &&
        Objects.equals(this.registryPrefix, messageRegistry_MessageRegistry.registryPrefix) &&
        Objects.equals(this.registryVersion, messageRegistry_MessageRegistry.registryVersion);
  }

  @Override
  public int hashCode() {
    return Objects.hash(atOdataType, actions, description, id, language, messages, name, oem, owningEntity, registryPrefix, registryVersion);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MessageRegistry_MessageRegistry {\n");
    
    sb.append("    atOdataType: ").append(toIndentedString(atOdataType)).append("\n");
    sb.append("    actions: ").append(toIndentedString(actions)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    language: ").append(toIndentedString(language)).append("\n");
    sb.append("    messages: ").append(toIndentedString(messages)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    oem: ").append(toIndentedString(oem)).append("\n");
    sb.append("    owningEntity: ").append(toIndentedString(owningEntity)).append("\n");
    sb.append("    registryPrefix: ").append(toIndentedString(registryPrefix)).append("\n");
    sb.append("    registryVersion: ").append(toIndentedString(registryVersion)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
