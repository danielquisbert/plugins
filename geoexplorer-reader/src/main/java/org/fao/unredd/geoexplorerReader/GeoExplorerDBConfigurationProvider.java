package org.fao.unredd.geoexplorerReader;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.geoladris.ConfigurationException;
import org.geoladris.DBUtils;
import org.geoladris.PersistenceException;
import org.geoladris.PortalRequestConfiguration;
import org.geoladris.config.ModuleConfigurationProvider;
import org.geoladris.config.PluginDescriptors;

import net.sf.json.JSON;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

public class GeoExplorerDBConfigurationProvider implements ModuleConfigurationProvider {

  public GeoExplorerDBConfigurationProvider() {}

  @Override
  public Map<String, JSONObject> getPluginConfig(PortalRequestConfiguration configurationContext,
      HttpServletRequest request) throws IOException {
    JSONObject conf = new JSONObject();
    conf.put("geoexplorer-layers", getGeoExplorerLayers(configurationContext, request));
    return Collections.singletonMap(PluginDescriptors.UNNAMED_GEOLADRIS_CORE_PLUGIN, conf);
  }

  private JSON getGeoExplorerLayers(PortalRequestConfiguration configurationContext,
      HttpServletRequest request) {
    try {
      String mapIdParameter = request.getParameter("mapId");
      int mapId;
      if (mapIdParameter != null) {
        try {
          mapId = Integer.parseInt(mapIdParameter);
        } catch (NumberFormatException e) {
          throw new ConfigurationException("mapId must be an integer");
        }
      } else {
        throw new ConfigurationException("mapId parameter must be configured");
      }
      return getGeoExplorerLayers(mapId);
    } catch (PersistenceException e) {
      throw new ConfigurationException("Cannot read geoexplorer database", e);
    }
  }

  private JSON getGeoExplorerLayers(final int mapId) throws PersistenceException {
    String config =
        DBUtils.processConnection("geoexplorer", new DBUtils.ReturningDBProcessor<String>() {

          @Override
          public String process(Connection connection) throws SQLException {
            PreparedStatement statement =
                connection.prepareStatement("select config from maps where id=?");
            statement.setInt(1, mapId);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
              return rs.getString(1);
            } else {
              return null;
            }
          }
        });

    return JSONSerializer.toJSON(config);
  }

  @Override
  public boolean canBeCached() {
    return false;
  }

}
