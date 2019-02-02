using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Text;
using System.Web;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

namespace TEST_magiword_dev_preview.ServiceHelper
{
    public static class GetJSONResponse
    {
        public static JObject GetJObject(string link)
        {
            JObject outerJObj;
            StringBuilder innerJson = new StringBuilder();

            try
            {
                WebRequest request = WebRequest.Create(link);
                WebResponse response = request.GetResponse();
                using (Stream stream = response.GetResponseStream())
                {
                    using (StreamReader reader = new StreamReader(stream))
                    {
                        string line = string.Empty;

                        while ((line = reader.ReadLine()) != null)
                        {
                            innerJson.Append(line);
                        }
                    }
                }
                response.Close();
                outerJObj = JObject.Parse(innerJson.ToString());
            }
            catch { outerJObj = null; }

            return outerJObj;
        }
    }
}