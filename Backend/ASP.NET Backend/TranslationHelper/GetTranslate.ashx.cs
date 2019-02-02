using Newtonsoft.Json.Linq;
using System;
using System.Collections;
using System.IO;
using System.Net;
using System.Text;
using System.Web;

namespace TEST_magiword_dev_preview.TranslationHelper
{

    public class GetTranslate : IHttpHandler
    {

        public void GetTranslation(HttpContext context)
        {
            // getting incoming data
            string word = context.Request["word"].Replace(" ", "%20");    // replacing spaces into url-like spaces
            string userguid = context.Request.Form["giud"];
            //TODO: here must be user login logic



            // validation, preparation and sending request to datasource services
            int key = ServiceHelper.GetKey.GetKeyByWord(word);
            if (key == int.MinValue)
                context.Response.Write("Exception: write word pls");
            string link = $"https://tempjavadictapp.herokuapp.com/dictionary?word={word}&key={key}&reqtype=simpleTranslation";
            JObject jo = ServiceHelper.GetJSONResponse.GetJObject(link);

            User.ActionToDB(userguid);

            // sending response to View
            dynamic json = new JObject();
            json.IsLoged = "true";
            json.result = jo;

            context.Response.Write(json);
        }



        public void ProcessRequest(HttpContext context)
        {
            GetTranslation(context);
        }

        public bool IsReusable {get { return false; }}
    }
}